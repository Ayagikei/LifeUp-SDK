@file:OptIn(ExperimentalSerializationApi::class)

package net.lifeupapp.lifeup.http.service

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.util.concurrent.CancellationException

internal const val DIAGNOSTICS_SCHEMA_VERSION = 1

internal const val DIAGNOSTICS_EXECUTION_MODE = "dispatchers_io"

internal const val DIAGNOSTICS_SLOW_THRESHOLD_MS = 10_000L

internal const val DIAGNOSTICS_ACTIVE_LIMIT = 64

internal const val DIAGNOSTICS_RECENT_LIMIT = 64

internal const val DIAGNOSTICS_PHASE_DISPATCHING = "dispatching"

internal const val DIAGNOSTICS_PHASE_EXECUTING = "executing"

internal const val DIAGNOSTICS_PHASE_COMPLETED = "completed"

internal const val DIAGNOSTICS_PHASE_FAILED = "failed"

internal const val DIAGNOSTICS_PHASE_CANCELLED_BEFORE_START = "cancelled_before_start"

private const val NANOS_PER_MILLISECOND = 1_000_000L

/**
 * Bounded metadata for synchronous provider attempts. Observation only:
 * nothing here cancels work, takes a permit, retries, or calls a provider.
 *
 * Call [dispatch] once, [begin] when the Cloud synchronous SDK region is
 * entered, and [complete] once. Put [complete] in that region's `finally`
 * so a disconnected client does not rewrite the real outcome. If the attempt
 * is cancelled before [begin], [complete] records `cancelled_before_start`.
 *
 * Active and recent storage each keep 64 rows. Overflow drops the oldest
 * stored row only. [ProviderDiagnosticsSnapshot.activeCount] still counts
 * every in-flight attempt, including ones whose active row was evicted.
 * `executing` means this process entered the synchronous SDK region,
 * including Binder or provider acquire. It does not mean the LifeUp app
 * started handling the call, and it does not identify a lock.
 */
internal class ProviderDiagnostics(
    private val nanoTime: () -> Long = System::nanoTime,
) {
    private val lock = Any()
    private var nextId = 1L
    private var activeCount = 0
    private val active = ArrayDeque<ProviderDiagnosticTrace>()
    private val recent = ArrayDeque<ProviderDiagnosticRecord>()

    fun dispatch(
        operation: String,
        route: String,
        batchSize: Int? = null,
    ): ProviderDiagnosticTrace = synchronized(lock) {
        val trace = ProviderDiagnosticTrace(
            id = nextId++,
            operation = diagnosticOperation(operation),
            route = sanitizeProviderRoute(route),
            batchSize = batchSize,
            dispatchNanos = nanoTime(),
        )
        activeCount += 1
        active.addLast(trace)
        if (active.size > DIAGNOSTICS_ACTIVE_LIMIT) active.removeFirst()
        trace
    }

    fun begin(trace: ProviderDiagnosticTrace) {
        synchronized(lock) {
            trace.begin(nanoTime())
        }
    }

    fun complete(trace: ProviderDiagnosticTrace, error: Throwable? = null) {
        synchronized(lock) {
            val now = nanoTime()
            if (!trace.finish(now, error)) return
            active.remove(trace)
            activeCount -= 1
            recent.addLast(trace.toRecord(now))
            while (recent.size > DIAGNOSTICS_RECENT_LIMIT) {
                recent.removeFirst()
            }
        }
    }

    /**
     * Immutable copy of in-process counters. Does not call a ContentProvider,
     * `/info`, or a database. [nanoTime] must stay non-blocking.
     */
    fun snapshot(
        cloudVersion: Int,
        cloudVersionName: String,
        androidApi: Int,
    ): ProviderDiagnosticsSnapshot = synchronized(lock) {
        val now = nanoTime()
        ProviderDiagnosticsSnapshot(
            cloudVersion = cloudVersion,
            cloudVersionName = cloudVersionName,
            androidApi = androidApi,
            activeCount = activeCount,
            activeTruncatedCount = activeCount - active.size,
            active = active.map { trace -> trace.toRecord(now) },
            recent = recent.toList(),
        )
    }
}

internal val sharedProviderDiagnostics: ProviderDiagnostics = ProviderDiagnostics()

/**
 * Handle for one attempt. Only [id] is part of the caller-facing surface.
 * Timing state is mutated under the recorder lock and is not a result payload.
 */
internal class ProviderDiagnosticTrace internal constructor(
    val id: Long,
    private val operation: String,
    private val route: String,
    private val batchSize: Int?,
    private val dispatchNanos: Long,
) {
    private var phase: String = DIAGNOSTICS_PHASE_DISPATCHING
    private var beginNanos: Long? = null
    private var endNanos: Long? = null
    private var errorType: String? = null
    private var finished: Boolean = false

    internal fun begin(now: Long) {
        if (finished || beginNanos != null) return
        beginNanos = now
        phase = DIAGNOSTICS_PHASE_EXECUTING
    }

    /** Returns false when this attempt was already completed. */
    internal fun finish(now: Long, error: Throwable?): Boolean {
        if (finished) return false
        finished = true
        endNanos = now
        errorType = error?.javaClass?.name
        phase = when {
            beginNanos == null && (error == null || error is CancellationException) ->
                DIAGNOSTICS_PHASE_CANCELLED_BEFORE_START
            error != null -> DIAGNOSTICS_PHASE_FAILED
            else -> DIAGNOSTICS_PHASE_COMPLETED
        }
        return true
    }

    internal fun toRecord(now: Long): ProviderDiagnosticRecord {
        val end = endNanos ?: now
        val elapsedMs = elapsedMillis(dispatchNanos, end)
        val started = beginNanos
        return ProviderDiagnosticRecord(
            id = id,
            operation = operation,
            api = diagnosticApi(route),
            route = route,
            batchSize = batchSize,
            phase = phase,
            elapsedMs = elapsedMs,
            dispatchMs = started?.let { elapsedMillis(dispatchNanos, it) },
            executionMs = started?.let { elapsedMillis(it, end) },
            slow = elapsedMs >= DIAGNOSTICS_SLOW_THRESHOLD_MS,
            errorType = errorType,
        )
    }

    override fun toString(): String = "ProviderDiagnosticTrace(id=$id)"
}

@Serializable
internal data class ProviderDiagnosticsSnapshot(
    @property:EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val schemaVersion: Int = DIAGNOSTICS_SCHEMA_VERSION,
    val cloudVersion: Int,
    val cloudVersionName: String,
    val androidApi: Int,
    @property:EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val executionMode: String = DIAGNOSTICS_EXECUTION_MODE,
    @property:EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val slowThresholdMs: Long = DIAGNOSTICS_SLOW_THRESHOLD_MS,
    val activeCount: Int,
    val activeTruncatedCount: Int,
    val active: List<ProviderDiagnosticRecord>,
    val recent: List<ProviderDiagnosticRecord>,
)

@Serializable
internal data class ProviderDiagnosticRecord(
    val id: Long,
    val operation: String,
    val api: String,
    val route: String,
    val batchSize: Int? = null,
    val phase: String,
    val elapsedMs: Long,
    val dispatchMs: Long? = null,
    val executionMs: Long? = null,
    val slow: Boolean,
    val errorType: String? = null,
)

private fun diagnosticOperation(operation: String): String = when (operation) {
    PROVIDER_OPERATION_READ, PROVIDER_OPERATION_COMMAND -> operation
    else -> "redacted"
}

internal fun diagnosticApi(route: String): String {
    val safeRoute = sanitizeProviderRoute(route)
    return DIAGNOSTIC_APIS[safeRoute] ?: "redacted"
}

private val DIAGNOSTIC_APIS = mapOf(
    "GET /info" to "info.get",
    "GET /tasks" to "tasks.list",
    "GET /tasks/{id}" to "tasks.list_category",
    "GET /smart_lists/{type}/tasks" to "tasks.list_smart",
    "GET /tasks_categories" to "tasks.list_categories",
    "GET /history" to "tasks.list_history",
    "GET /items" to "items.list",
    "GET /items/{listId}" to "items.list_category",
    "GET /items_categories" to "items.list_categories",
    "GET /skills" to "skills.list",
    "GET /skill_groups" to "skills.list_groups",
    "GET /achievements" to "achievements.list",
    "GET /achievements/{id}" to "achievements.list_category",
    "GET /achievement_categories" to "achievements.list_categories",
    "GET /achievement_conditions/{id}" to "achievements.list_conditions",
    "GET /feelings" to "feelings.list",
    "GET /synthesis" to "synthesis.list",
    "GET /synthesis/{id}" to "synthesis.get",
    "GET /synthesis_categories" to "synthesis.list_categories",
    "GET /synthesis_categories/{id}" to "synthesis.list_category",
    "GET /pomodoro_records" to "pomodoro.list_records",
    "GET /coin" to "coin.get",
    "GET /coin_records" to "coin.list_records",
    "GET /inventory_records" to "inventory.list_records",
    "GET /exp_records" to "exp.list_records",
    "GET /step_records" to "steps.list_records",
    "GET /level_defines" to "levels.get",
    "GET /statistics" to "statistics.get",
    "GET /data/export" to "data.export",
    "GET /api/contentprovider" to "api.command",
    "POST /api/contentprovider" to "api.command",
)

private fun elapsedMillis(startNanos: Long, endNanos: Long): Long {
    val delta = endNanos - startNanos
    if (delta <= 0L) return 0L
    return delta / NANOS_PER_MILLISECOND
}
