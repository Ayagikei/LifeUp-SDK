package net.lifeupapp.lifeup.http.service

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.queryString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout
import net.lifeupapp.lifeup.api.exception.CloudBusyException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

internal const val CONCURRENCY_LIMIT_HEADER = "X-LifeUp-Concurrency"

internal const val CONCURRENCY_LIMITED_VALUE = "limited"

internal const val HEAVY_ADMISSION_PERMITS = 4

internal const val COMMAND_ADMISSION_PERMITS = 2

internal const val PROVIDER_ADMISSION_WAIT_MS = 20_000L

/**
 * Route templates that must not wait behind heavy provider reads.
 * Any other protected read uses the heavy permit pool.
 */
internal val LIGHT_PROVIDER_ROUTES: Set<String> = setOf(
    "GET /info",
    "GET /coin",
    "GET /tasks_categories",
    "GET /items_categories",
    "GET /achievement_categories",
    "GET /synthesis_categories",
    "GET /synthesis_categories/{id}",
    "GET /skill_groups",
    "GET /level_defines",
)

/**
 * Opt-in gate for one Cloud request. Missing or non-`limited` values stay on
 * the direct [ProviderExecution.run] path.
 */
internal fun ApplicationCall.requestsLimitedConcurrency(): Boolean {
    return request.headers[CONCURRENCY_LIMIT_HEADER]
        ?.equals(CONCURRENCY_LIMITED_VALUE, ignoreCase = true) == true
}

/** Method plus path plus raw query. Commands do not use this key. */
internal fun ApplicationCall.providerCoalesceKey(): String {
    val path = request.path()
    val query = request.queryString()
    val target = if (query.isEmpty()) path else "$path?$query"
    return "${request.httpMethod.value} $target"
}

/**
 * Protects provider calls when the caller opted in.
 *
 * Heavy reads take one of [HEAVY_ADMISSION_PERMITS]. Commands take one of
 * [COMMAND_ADMISSION_PERMITS] for the whole batch. Light reads never take a
 * permit. Waiting is bounded by [waitTimeoutMs]; on timeout the block is not
 * called and the caller gets [CloudBusyException].
 *
 * The permit is acquired before the synchronous block and released only after
 * that block returns. Client cancellation does not interrupt blocking Binder
 * work and must not free the permit early. Cancellation while still queued
 * does not start the block.
 *
 * Protected reads with the same [providerCoalesceKey] share one in-flight
 * call on [scope], which is not a child of the HTTP request. A leader
 * disconnect therefore does not cancel the shared call while another waiter
 * remains. The map entry is removed when the shared call finishes. Nothing
 * is cached after that. A busy timeout fails every waiter of that flight.
 *
 * [scope] must not use [kotlinx.coroutines.Dispatchers.Unconfined]. The flight
 * is started outside the caller's job so disconnect cannot cancel it directly.
 */
internal class ProviderAdmission(
    heavyPermits: Int = HEAVY_ADMISSION_PERMITS,
    commandPermits: Int = COMMAND_ADMISSION_PERMITS,
    private val waitTimeoutMs: Long = PROVIDER_ADMISSION_WAIT_MS,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val nanoTime: () -> Long = System::nanoTime,
) {
    private val heavySemaphore = Semaphore(heavyPermits)
    private val commandSemaphore = Semaphore(commandPermits)
    private val gate = Any()
    private val inFlight = HashMap<String, Flight>()

    internal fun heavyAvailablePermits(): Int = heavySemaphore.availablePermits

    internal fun commandAvailablePermits(): Int = commandSemaphore.availablePermits

    internal fun waitersFor(coalesceKey: String): Int {
        synchronized(gate) {
            return inFlight[coalesceKey]?.waiterCount() ?: 0
        }
    }

    /** Cancels queued flights owned by this gate. In-flight blocking work still finishes. */
    internal fun close() {
        scope.cancel()
    }

    internal suspend fun <T> read(
        coalesceKey: String,
        route: String,
        execution: ProviderExecution,
        block: () -> T,
    ): T {
        val heavy = route !in LIGHT_PROVIDER_ROUTES
        val (flight, shared) = beginFlight(coalesceKey, route, execution, heavy, block)
        try {
            @Suppress("UNCHECKED_CAST")
            return if (shared) {
                execution.awaitShared(route) { flight.deferred.await() as T }
            } else {
                flight.deferred.await() as T
            }
        } finally {
            if (flight.detach()) {
                flight.job?.cancel()
            }
        }
    }

    internal suspend fun <T> command(
        route: String,
        batchSize: Int,
        execution: ProviderExecution,
        block: () -> T,
    ): T {
        var acquired = false
        var handedOff = false
        val waitStart = nanoTime()
        try {
            acquired = commandSemaphore.tryAcquireWithin(waitTimeoutMs)
            val queueMs = elapsedMs(waitStart)
            if (!acquired) {
                execution.recordUnstarted(
                    PROVIDER_OPERATION_COMMAND,
                    route,
                    batchSize,
                    queueMs,
                    CloudBusyException(),
                )
                throw CloudBusyException()
            }
            coroutineContext.ensureActive()
            handedOff = true
            return execution.run(
                operation = PROVIDER_OPERATION_COMMAND,
                route = route,
                batchSize = batchSize,
                limited = true,
                queueMs = queueMs,
                block = block,
            )
        } catch (error: Throwable) {
            if (!handedOff && error !is CloudBusyException) {
                execution.recordUnstarted(
                    PROVIDER_OPERATION_COMMAND,
                    route,
                    batchSize,
                    elapsedMs(waitStart),
                    error,
                )
            }
            throw error
        } finally {
            if (acquired) commandSemaphore.release()
        }
    }

    private fun <T> beginFlight(
        key: String,
        route: String,
        execution: ProviderExecution,
        heavy: Boolean,
        block: () -> T,
    ): Pair<Flight, Boolean> {
        val flightToStart = synchronized(gate) {
            val existing = inFlight[key]
            if (existing != null && existing.tryJoin()) {
                return existing to true
            }
            if (existing != null && inFlight[key] === existing) {
                inFlight.remove(key)
            }
            val created = Flight()
            check(created.tryJoin())
            inFlight[key] = created
            created
        }
        val job = scope.launch(start = CoroutineStart.LAZY) {
            runFlight(flightToStart, key, route, execution, heavy, block)
        }
        flightToStart.job = job
        // A cancel arriving before this coroutine is scheduled never enters
        // runFlight, which is the only place that drops the map entry.
        job.invokeOnCompletion { cause ->
            if (cause == null) return@invokeOnCompletion
            removeFlight(key, flightToStart)
            if (!flightToStart.deferred.isCompleted) {
                flightToStart.deferred.completeExceptionally(cause)
            }
        }
        job.start()
        return flightToStart to false
    }

    private suspend fun <T> runFlight(
        flight: Flight,
        key: String,
        route: String,
        execution: ProviderExecution,
        heavy: Boolean,
        block: () -> T,
    ) {
        var acquired = false
        var handedOff = false
        val waitStart = nanoTime()
        try {
            val queueMs = if (heavy) {
                acquired = heavySemaphore.tryAcquireWithin(waitTimeoutMs)
                val waited = elapsedMs(waitStart)
                if (!acquired) {
                    execution.recordUnstarted(
                        PROVIDER_OPERATION_READ,
                        route,
                        batchSize = null,
                        queueMs = waited,
                        error = CloudBusyException(),
                    )
                    throw CloudBusyException()
                }
                waited
            } else {
                0L
            }
            coroutineContext.ensureActive()
            if (!flight.markStarted()) {
                throw CancellationException("LifeUp Cloud admission cancelled before start")
            }
            handedOff = true
            val value = execution.run(
                operation = PROVIDER_OPERATION_READ,
                route = route,
                limited = true,
                queueMs = queueMs,
                block = block,
            )
            removeFlight(key, flight)
            flight.deferred.complete(value)
        } catch (error: Throwable) {
            try {
                if (!handedOff && error !is CloudBusyException) {
                    execution.recordUnstarted(
                        PROVIDER_OPERATION_READ,
                        route,
                        batchSize = null,
                        queueMs = elapsedMs(waitStart),
                        error = error,
                    )
                }
            } finally {
                removeFlight(key, flight)
                flight.deferred.completeExceptionally(error)
            }
        } finally {
            if (acquired) heavySemaphore.release()
        }
    }

    private fun removeFlight(key: String, flight: Flight) {
        synchronized(gate) {
            if (inFlight[key] === flight) {
                inFlight.remove(key)
            }
        }
    }

    private fun elapsedMs(startNanos: Long): Long {
        val delta = nanoTime() - startNanos
        if (delta <= 0L) return 0L
        return delta / 1_000_000L
    }
}

internal val sharedProviderAdmission: ProviderAdmission = ProviderAdmission()

/**
 * Acquires one permit or returns false when [timeoutMs] elapses.
 * A permit is never left held when this function throws.
 * If the deadline fires after the permit was taken, the caller keeps it.
 */
private suspend fun Semaphore.tryAcquireWithin(timeoutMs: Long): Boolean {
    val held = AtomicBoolean(false)
    try {
        withTimeout(timeoutMs) {
            acquire()
            held.set(true)
        }
        return true
    } catch (cancelled: CancellationException) {
        val ownsPermit = held.get()
        val timedOut = cancelled is TimeoutCancellationException
        if (timedOut && ownsPermit) return true
        if (ownsPermit) release()
        if (timedOut) return false
        throw cancelled
    }
}

/**
 * One protected read. [deferred] is not a child of any HTTP request.
 * Join fails once the flight is closed so a late waiter cannot attach to a
 * call that was cancelled before start.
 */
private class Flight {
    val deferred = CompletableDeferred<Any?>()
    @Volatile var job: Job? = null
    private val stateLock = Any()
    private var waiters = 0
    private var started = false
    private var closed = false

    fun tryJoin(): Boolean = synchronized(stateLock) {
        if (closed) return false
        waiters += 1
        true
    }

    fun markStarted(): Boolean = synchronized(stateLock) {
        if (closed) return false
        started = true
        true
    }

    /** @return true when this was the last waiter and the block has not started */
    fun detach(): Boolean = synchronized(stateLock) {
        waiters -= 1
        if (waiters == 0 && !started && !closed) {
            closed = true
            true
        } else {
            false
        }
    }

    fun waiterCount(): Int = synchronized(stateLock) { waiters }
}
