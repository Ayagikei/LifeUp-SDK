package net.lifeupapp.lifeup.http.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.lifeupapp.lifeup.http.vo.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

class ProviderExecutionTest {

    @Test(timeout = 10_000)
    fun heldCallbacks_letFiveReadsAndThreeCommandsEnterTogether() = runBlocking {
        val execution = ProviderExecution()
        val readsEntered = CountDownLatch(5)
        val commandsEntered = CountDownLatch(3)
        val release = CountDownLatch(1)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val reads = List(5) { index ->
                scope.launch {
                    val value = execution.run(PROVIDER_OPERATION_READ, "GET /info") {
                        readsEntered.countDown()
                        awaitIgnoringInterrupt(release)
                        "read-$index"
                    }
                    check(value == "read-$index")
                }
            }
            val commands = List(3) { index ->
                scope.launch {
                    val value = execution.run(
                        PROVIDER_OPERATION_COMMAND,
                        "POST /api/contentprovider",
                        batchSize = index + 1,
                    ) {
                        commandsEntered.countDown()
                        awaitIgnoringInterrupt(release)
                        "command-$index"
                    }
                    check(value == "command-$index")
                }
            }
            assertTrue(
                "reads did not all enter while earlier callbacks were still held",
                readsEntered.await(5, TimeUnit.SECONDS),
            )
            assertTrue(
                "commands did not all enter while earlier callbacks were still held",
                commandsEntered.await(5, TimeUnit.SECONDS),
            )
            release.countDown()
            withTimeout(5_000) { (reads + commands).forEach { it.join() } }
        } finally {
            release.countDown()
            scope.cancel()
        }
    }

    @Test(timeout = 10_000)
    fun blockingWork_runsOnTheInjectedIoDispatcher() = runBlocking {
        val callerDispatcher = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "provider-caller").apply { isDaemon = true }
        }.asCoroutineDispatcher()
        val ioDispatcher = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "provider-execution-io").apply { isDaemon = true }
        }.asCoroutineDispatcher()
        try {
            val worker = withContext(callerDispatcher) {
                ProviderExecution(ioDispatcher = ioDispatcher).run(PROVIDER_OPERATION_READ, "GET /info") {
                    Thread.currentThread().name
                }
            }
            assertTrue("provider work ran on caller thread $worker", worker.startsWith("provider-execution-io"))
            assertFalse(worker.startsWith("provider-caller"))
        } finally {
            callerDispatcher.close()
            ioDispatcher.close()
        }
    }

    @Test(timeout = 10_000)
    fun failures_propagateAndLaterCallsStillRun() = runBlocking {
        val execution = ProviderExecution()
        repeat(5) { index ->
            val thrown = runCatching {
                execution.run(PROVIDER_OPERATION_READ, "GET /info") {
                    throw IllegalStateException("boom-$index")
                }
            }.exceptionOrNull()
            assertTrue(thrown is IllegalStateException)
            assertEquals("boom-$index", thrown?.message)
        }
        assertEquals(
            "after-exception",
            execution.run(PROVIDER_OPERATION_READ, "GET /info") { "after-exception" },
        )

        val error = runCatching {
            execution.run(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", batchSize = 1) {
                throw ProbeError()
            }
        }.exceptionOrNull()
        assertTrue(error is ProbeError)
        assertEquals(
            "after-error",
            execution.run(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", batchSize = 1) {
                "after-error"
            },
        )
    }

    @Test(timeout = 10_000)
    fun cancellationBeforeDispatch_doesNotInvokeCallback() = runBlocking {
        val dispatcher = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "provider-execution-test").apply { isDaemon = true }
        }.asCoroutineDispatcher()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val release = CountDownLatch(1)
        val logs = mutableListOf<String>()
        try {
            val execution = ProviderExecution(
                ioDispatcher = dispatcher,
                log = { line -> logs.add(line) },
            )
            val entered = CountDownLatch(1)
            val blocker = scope.launch {
                execution.run(PROVIDER_OPERATION_READ, "GET /hold") {
                    entered.countDown()
                    awaitIgnoringInterrupt(release)
                    "held"
                }
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            val executed = AtomicBoolean(false)
            val cancelled = scope.launch {
                execution.run(PROVIDER_OPERATION_READ, "GET /info") {
                    executed.set(true)
                    "should-not-run"
                }
            }
            cancelled.cancel()
            release.countDown()
            withTimeout(5_000) {
                blocker.join()
                cancelled.join()
            }
            withContext(dispatcher) { }
            assertFalse(executed.get())
            assertTrue(cancelled.isCancelled)
            assertTrue(logs.none { "GET /info" in it })
            assertEquals("ok", execution.run(PROVIDER_OPERATION_READ, "GET /info") { "ok" })
        } finally {
            release.countDown()
            scope.cancel()
            dispatcher.close()
        }
    }

    @Test(timeout = 10_000)
    fun cancellationDuringBlockingCall_finishesOnceWithoutInterrupt() = runBlocking {
        val execution = ProviderExecution()
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val finished = AtomicBoolean(false)
        val interrupted = AtomicBoolean(false)
        val runs = AtomicInteger()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val job = scope.launch {
                execution.run(PROVIDER_OPERATION_READ, "GET /info") {
                    runs.incrementAndGet()
                    started.countDown()
                    interrupted.set(awaitAndRecordInterrupt(release))
                    finished.set(true)
                    "done"
                }
            }
            assertTrue(started.await(5, TimeUnit.SECONDS))
            job.cancel()
            val extraReads = CountDownLatch(5)
            val extraCommands = CountDownLatch(3)
            val extras = List(5) {
                scope.launch {
                    execution.run(PROVIDER_OPERATION_READ, "GET /tasks") {
                        extraReads.countDown()
                        "extra-read"
                    }
                }
            } + List(3) {
                scope.launch {
                    execution.run(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", batchSize = 1) {
                        extraCommands.countDown()
                        "extra-command"
                    }
                }
            }
            assertTrue(extraReads.await(5, TimeUnit.SECONDS))
            assertTrue(extraCommands.await(5, TimeUnit.SECONDS))
            withTimeout(5_000) { extras.forEach { it.join() } }
            assertFalse(finished.get())
            assertEquals(1, runs.get())
            release.countDown()
            withTimeout(5_000) { job.join() }
            assertTrue(job.isCancelled)
            assertTrue(finished.get())
            assertFalse(interrupted.get())
            assertEquals(1, runs.get())
        } finally {
            release.countDown()
            scope.cancel()
        }
    }

    @Test(timeout = 10_000)
    fun logs_recordSanitizedTimingWithoutSecretsOrBusy() = runBlocking {
        var ticks = 0L
        val logs = mutableListOf<String>()
        val execution = ProviderExecution(
            log = { line -> logs.add(line) },
            nanoTime = {
                ticks += 2_000_000L
                ticks
            },
        )
        assertEquals("ok", execution.run(PROVIDER_OPERATION_READ, "GET /info") { "ok" })
        assertEquals(
            listOf(PROVIDER_LOG_BEGIN, PROVIDER_LOG_END),
            logs.map { it.substringBefore(' ') },
        )
        assertTrue(logs.all { it.contains("operation=read") && it.contains("route=\"GET /info\"") })
        assertTrue(logs.none { "lane=" in it || "provider_busy" in it || "provider_http_admission" in it })
        assertTrue(logs.last().contains("durationMs=2"))

        logs.clear()
        val failure = runCatching {
            execution.run(
                PROVIDER_OPERATION_COMMAND,
                "lifeup://api/reward?token=super-secret",
                batchSize = 1,
            ) {
                throw IllegalStateException("lifeup://api/reward?token=super-secret")
            }
        }.exceptionOrNull()
        assertTrue(failure is IllegalStateException)
        assertEquals(
            listOf(PROVIDER_LOG_BEGIN, PROVIDER_LOG_END),
            logs.map { it.substringBefore(' ') },
        )
        assertTrue(logs.all { line ->
            "super-secret" !in line &&
                "lifeup://" !in line &&
                "token" !in line &&
                line.contains("route=\"redacted\"") &&
                line.contains("operation=command") &&
                line.contains("batchSize=1") &&
                "lane=" !in line
        })
        assertTrue(logs.last().contains("durationMs=2"))
        assertEquals("redacted", sanitizeProviderRoute("bad\"route"))
        assertEquals("redacted", sanitizeProviderRoute("a\nb"))
        assertEquals("GET /info", sanitizeProviderRoute("GET /info"))
    }

    @Test
    fun commandBatch_preservesOrderPartialPayloadsAndStopsOnThrow() {
        val seen = mutableListOf<String>()
        val response = contentProviderPostResults(listOf("ok", "bad"), "tail") { url ->
            seen += url
            JsonObject(mapOf("status" to JsonPrimitive(if (url == "bad") "error" else "success")))
        }
        assertEquals(listOf("ok", "bad", "tail"), seen)
        assertEquals(HttpResponse.SUCCESS, response.code)
        assertEquals(listOf("ok", "bad", "tail"), response.data!!.map { it.url })
        assertEquals(JsonPrimitive("error"), response.data!![1].result!!["status"])
        assertEquals(JsonPrimitive("success"), response.data!![2].result!!["status"])

        var calls = 0
        val thrown = runCatching {
            contentProviderPostResults(listOf("a", "b", "c"), "d") { url ->
                calls += 1
                if (url == "b") throw IllegalStateException("boom")
                null
            }
        }.exceptionOrNull()
        assertTrue(thrown is IllegalStateException)
        assertEquals(2, calls)

        val missing = contentProviderGetResults(null) { throw IllegalStateException("should-not-run") }
        assertEquals(HttpResponse.ERROR, missing.code)
        assertEquals("No url found", missing.message)
        assertNull(missing.data)

        val got = contentProviderGetResults(listOf("z", "a")) { url ->
            JsonObject(mapOf("n" to JsonPrimitive(url)))
        }
        assertEquals(listOf("z", "a"), got.data!!.map { it.url })
    }

    @Test(timeout = 10_000)
    fun commandBatch_staysOrderedOnOneThreadWhileAnotherCommandEnters() = runBlocking {
        val execution = ProviderExecution()
        val order = mutableListOf<String>()
        val threads = mutableListOf<String>()
        val enteredFirst = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondEntered = CountDownLatch(1)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val batch = scope.launch {
                execution.run(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", batchSize = 4) {
                    contentProviderPostResults(listOf("u1", "u2", "u3"), "u4") { url ->
                        synchronized(order) {
                            order.add(url)
                            threads.add(Thread.currentThread().name)
                        }
                        if (url == "u1") {
                            enteredFirst.countDown()
                            awaitIgnoringInterrupt(releaseFirst)
                        }
                        null
                    }
                }
            }
            assertTrue(enteredFirst.await(5, TimeUnit.SECONDS))
            val second = scope.launch {
                execution.run(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", batchSize = 1) {
                    secondEntered.countDown()
                    "second"
                }
            }
            assertTrue(
                "second command waited for the held batch",
                secondEntered.await(5, TimeUnit.SECONDS),
            )
            withTimeout(5_000) { second.join() }
            assertEquals(listOf("u1"), synchronized(order) { order.toList() })
            releaseFirst.countDown()
            withTimeout(5_000) { batch.join() }
            assertEquals(listOf("u1", "u2", "u3", "u4"), synchronized(order) { order.toList() })
            assertEquals(1, synchronized(threads) { threads.toSet().size })
        } finally {
            releaseFirst.countDown()
            scope.cancel()
        }
    }

    @Test
    fun catchProviderFailure_rethrowsCancellationAndErrorOnly() {
        val cancelled = runCatching {
            catchProviderFailure<String> { throw CancellationException("stop") }
        }.exceptionOrNull()
        assertTrue(cancelled is CancellationException)
        assertEquals("stop", cancelled?.message)

        val error = runCatching {
            catchProviderFailure<String> { throw ProbeError() }
        }.exceptionOrNull()
        assertTrue(error is ProbeError)

        val failed = catchProviderFailure<String> { throw IllegalStateException("boom") }
        assertTrue(failed.exceptionOrNull() is IllegalStateException)
        assertEquals("ok", catchProviderFailure { "ok" }.getOrThrow())
    }

    private class ProbeError : Error("probe")
}

private fun awaitIgnoringInterrupt(latch: CountDownLatch) {
    if (awaitAndRecordInterrupt(latch)) {
        Thread.currentThread().interrupt()
    }
}

private fun awaitAndRecordInterrupt(latch: CountDownLatch): Boolean {
    var interrupted = false
    while (latch.count > 0) {
        try {
            if (latch.await(200, TimeUnit.MILLISECONDS)) break
        } catch (_: InterruptedException) {
            interrupted = true
        }
    }
    return interrupted || Thread.currentThread().isInterrupted
}
