package net.lifeupapp.lifeup.http.service

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import net.lifeupapp.lifeup.http.vo.CallUrlResult
import net.lifeupapp.lifeup.http.vo.HttpResponse
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/** Log label for query-style ContentProvider reads. Not a concurrency lane. */
internal const val PROVIDER_OPERATION_READ = "read"

/** Log label for a URL-scheme ContentProvider batch. Not a concurrency lane. */
internal const val PROVIDER_OPERATION_COMMAND = "command"

internal const val PROVIDER_LOG_BEGIN = "provider_begin"

internal const val PROVIDER_LOG_END = "provider_end"

private const val REDACTED_ROUTE = "redacted"

/**
 * Moves one synchronous ContentProvider call onto [ioDispatcher].
 *
 * [operation] is only a sanitized log label. This does not limit concurrency,
 * queue work, or turn caller cancellation into a thread interrupt. Cancellation
 * before [block] starts skips the callback. An in-flight blocking callback runs
 * to completion; cancellation and [Error] still propagate after it returns.
 */
internal class ProviderExecution(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val log: (String) -> Unit = { message ->
        Logger.getLogger("LifeUp-Http").info(message)
    },
    private val nanoTime: () -> Long = System::nanoTime,
    private val diagnostics: ProviderDiagnostics = sharedProviderDiagnostics,
) {
    suspend fun <T> run(
        operation: String,
        route: String,
        batchSize: Int? = null,
        block: () -> T,
    ): T {
        coroutineContext.ensureActive()
        val trace = diagnostics.dispatch(operation, route, batchSize)
        var entered = false
        try {
            return withContext(ioDispatcher) {
                coroutineContext.ensureActive()
                entered = true
                diagnostics.begin(trace)
                val startedAt = nanoTime()
                var failure: Throwable? = null
                try {
                    logEvent(PROVIDER_LOG_BEGIN, operation, route, batchSize, durationMs = null)
                    val result = block()
                    failure = (result as? Result<*>)?.exceptionOrNull()
                    result
                } catch (error: Throwable) {
                    failure = error
                    throw error
                } finally {
                    // Record actual synchronous completion, not the client's waiting outcome.
                    diagnostics.complete(trace, failure)
                    val durationMs = (nanoTime() - startedAt) / 1_000_000
                    logEvent(PROVIDER_LOG_END, operation, route, batchSize, durationMs)
                }
            }
        } catch (error: Throwable) {
            if (!entered) diagnostics.complete(trace, error)
            throw error
        }
    }

    private fun logEvent(
        event: String,
        operation: String,
        route: String,
        batchSize: Int?,
        durationMs: Long?,
    ) {
        val line = buildString {
            append(event)
            append(" operation=")
            append(sanitizeProviderRoute(operation))
            append(" route=\"")
            append(sanitizeProviderRoute(route))
            append('"')
            if (batchSize != null) {
                append(" batchSize=")
                append(batchSize)
            }
            if (durationMs != null) {
                append(" durationMs=")
                append(durationMs)
            }
        }
        log(line)
    }
}

internal val sharedProviderExecution: ProviderExecution = ProviderExecution()

internal fun sanitizeProviderRoute(route: String): String {
    if (route.any { it == '"' || it == '\n' || it == '\r' }) return REDACTED_ROUTE
    val lower = route.lowercase()
    if ("://" in route || "token" in lower || "?" in route || "&" in route || "=" in route) {
        return REDACTED_ROUTE
    }
    return route
}

internal suspend fun <T> ApplicationCall.respondProviderResult(
    route: String,
    block: () -> Result<T>,
    execution: ProviderExecution = sharedProviderExecution,
    onFailure: (suspend (Throwable) -> Unit)? = null,
    onSuccess: suspend (T) -> Unit,
) {
    val result = execution.run(PROVIDER_OPERATION_READ, route, block = block)
    result.onSuccess { value -> onSuccess(value) }
        .onFailure { error ->
            onFailure?.invoke(error)
            respond(HttpResponse.error<String>(error))
        }
}

/**
 * Preserves provider error envelopes without swallowing cancellation or [Error].
 * [kotlin.runCatching] catches both; ContentProvider routes must not.
 * Not inline: it is invoked inside the provider lambda, and an inlined return
 * would leave that route instead of becoming the block result.
 */
internal fun <T> catchProviderFailure(block: () -> T): Result<T> {
    try {
        return Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Error) {
        throw error
    } catch (exception: Exception) {
        return Result.failure(exception)
    }
}

internal fun providerCommandBatchSize(urls: List<String>?, url: String?): Int {
    return (urls?.size ?: 0) + if (url != null) 1 else 0
}

/** GET /api/contentprovider: null urls is "No url found"; order follows the query list. */
internal fun contentProviderGetResults(
    urls: List<String>?,
    call: (String) -> JsonObject?,
): HttpResponse<List<CallUrlResult>?> {
    return urls?.map { url -> CallUrlResult(url, call(url)) }?.wrapAsResponse()
        ?: HttpResponse.error("No url found")
}

/**
 * POST /api/contentprovider. Every `urls` entry runs first, then `url`.
 * A thrown call aborts the rest; returned bundles are kept as-is, including failures.
 */
internal fun contentProviderPostResults(
    urls: List<String>?,
    url: String?,
    call: (String) -> JsonObject?,
): HttpResponse<List<CallUrlResult>?> {
    val resultList = (urls?.map { item -> CallUrlResult(item, call(item)) } ?: emptyList()).toMutableList()
    if (url != null) {
        resultList.add(CallUrlResult(url, call(url)))
    }
    return resultList.wrapAsResponse()
}

internal suspend fun ApplicationCall.respondContentProviderGet(
    urls: List<String>?,
    execution: ProviderExecution = sharedProviderExecution,
    call: (String) -> JsonObject?,
) {
    if (urls == null) {
        respond(HttpResponse.error<List<CallUrlResult>>("No url found"))
        return
    }
    if (urls.isEmpty()) {
        respond(emptyList<CallUrlResult>().wrapAsResponse())
        return
    }
    respondProviderCommand(PROVIDER_OPERATION_COMMAND, "GET /api/contentprovider", urls.size, execution) {
        contentProviderGetResults(urls, call)
    }
}

internal suspend fun ApplicationCall.respondContentProviderPost(
    body: RawQueryVo,
    execution: ProviderExecution = sharedProviderExecution,
    call: (String) -> JsonObject?,
) {
    val batchSize = providerCommandBatchSize(body.urls, body.url)
    if (batchSize == 0) {
        respond(emptyList<CallUrlResult>().wrapAsResponse())
        return
    }
    respondProviderCommand(
        PROVIDER_OPERATION_COMMAND,
        "POST /api/contentprovider",
        batchSize,
        execution,
    ) {
        contentProviderPostResults(body.urls, body.url, call)
    }
}

private suspend fun ApplicationCall.respondProviderCommand(
    operation: String,
    route: String,
    batchSize: Int,
    execution: ProviderExecution,
    block: () -> HttpResponse<List<CallUrlResult>?>,
) {
    val result = execution.run(operation, route, batchSize) { catchProviderFailure(block) }
    result.onSuccess { body -> respond(body) }
        .onFailure { error -> respond(HttpResponse.error<String>(error)) }
}
