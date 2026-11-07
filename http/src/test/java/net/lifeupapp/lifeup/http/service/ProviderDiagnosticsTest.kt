package net.lifeupapp.lifeup.http.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ProviderDiagnosticsTest {

    @Test
    fun clock_reportsStagesElapsedAndSlowThreshold() {
        var nanos = 0L
        val diagnostics = ProviderDiagnostics { nanos }
        val read = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /tasks")

        nanos = millis(1_500)
        val dispatching = diagnostics.snapshot(1, "1", 34).active.single()
        assertEquals(read.id, dispatching.id)
        assertEquals("read", dispatching.operation)
        assertEquals("GET /tasks", dispatching.route)
        assertEquals("dispatching", dispatching.phase)
        assertEquals(1_500L, dispatching.elapsedMs)
        assertNull(dispatching.dispatchMs)
        assertNull(dispatching.executionMs)
        assertNull(dispatching.batchSize)
        assertNull(dispatching.errorType)
        assertFalse(dispatching.slow)

        nanos = millis(9_999)
        val underThreshold = diagnostics.snapshot(1, "1", 34).active.single()
        assertEquals(9_999L, underThreshold.elapsedMs)
        assertFalse(underThreshold.slow)
        assertEquals("dispatching", underThreshold.phase)

        nanos = millis(10_000)
        val slowDispatch = diagnostics.snapshot(1, "1", 34).active.single()
        assertEquals(10_000L, slowDispatch.elapsedMs)
        assertTrue(slowDispatch.slow)
        assertNull(slowDispatch.executionMs)

        nanos = millis(12_000)
        diagnostics.begin(read)
        nanos = millis(15_500)
        val executing = diagnostics.snapshot(1, "1", 34).active.single()
        assertEquals("executing", executing.phase)
        assertEquals(12_000L, executing.dispatchMs)
        assertEquals(3_500L, executing.executionMs)
        assertEquals(15_500L, executing.elapsedMs)
        assertTrue(executing.slow)

        nanos = millis(16_000)
        diagnostics.complete(read)
        val done = diagnostics.snapshot(1, "1", 34)
        assertEquals(0, done.activeCount)
        assertTrue(done.active.isEmpty())
        val finished = done.recent.single()
        assertEquals("completed", finished.phase)
        assertEquals(12_000L, finished.dispatchMs)
        assertEquals(4_000L, finished.executionMs)
        assertEquals(16_000L, finished.elapsedMs)
        assertTrue(finished.slow)
        assertNull(finished.errorType)

        nanos = millis(90_000)
        assertEquals(16_000L, done.recent.single().elapsedMs)
        assertEquals(1, done.recent.size)

        val command = diagnostics.dispatch(
            PROVIDER_OPERATION_COMMAND,
            "POST /api/contentprovider",
            batchSize = 2,
        )
        diagnostics.begin(command)
        diagnostics.complete(command)
        val commandRow = diagnostics.snapshot(1, "1", 34).recent.last()
        assertEquals("command", commandRow.operation)
        assertEquals("POST /api/contentprovider", commandRow.route)
        assertEquals(2 as Int?, commandRow.batchSize)
        assertEquals("completed", commandRow.phase)
        assertEquals(0L, commandRow.dispatchMs)
        assertEquals(0L, commandRow.executionMs)
        assertEquals(0L, commandRow.elapsedMs)
        assertFalse(commandRow.slow)
        assertEquals(1, done.recent.size)
    }

    @Test
    fun cancelledBeforeStart_andSyncErrors_keepClassOnly() {
        var nanos = 0L
        val diagnostics = ProviderDiagnostics { nanos }
        val secret = "raw-token-value"

        nanos = 0L
        val quiet = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /tasks")
        nanos = millis(400)
        diagnostics.complete(quiet)

        nanos = millis(1_000)
        val cancelled = diagnostics.dispatch(
            PROVIDER_OPERATION_COMMAND,
            "POST /api/contentprovider",
            batchSize = 2,
        )
        val cancellation = DispatchCancellation("Bearer $secret")
        nanos = millis(1_500)
        diagnostics.complete(cancelled, cancellation)

        nanos = millis(2_000)
        val earlyFailure = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /items")
        nanos = millis(2_200)
        diagnostics.complete(earlyFailure, IllegalStateException(secret))

        nanos = millis(3_000)
        val started = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /skills")
        nanos = millis(3_800)
        diagnostics.begin(started)
        nanos = millis(4_000)
        val startedCancellation = CancellationException(secret)
        diagnostics.complete(started, startedCancellation)

        nanos = millis(5_000)
        val sync = diagnostics.dispatch(
            PROVIDER_OPERATION_COMMAND,
            "POST /api/contentprovider",
            batchSize = 1,
        )
        nanos = millis(5_100)
        diagnostics.begin(sync)
        nanos = millis(5_350)
        val syncError = IllegalStateException(
            "task=$secret",
            IllegalArgumentException("cause=$secret"),
        )
        diagnostics.complete(sync, syncError)

        val snapshot = diagnostics.snapshot(1, "1", 34)
        assertEquals(0, snapshot.activeCount)
        assertEquals(
            listOf(quiet.id, cancelled.id, earlyFailure.id, started.id, sync.id),
            snapshot.recent.map { it.id },
        )
        val rows = snapshot.recent

        assertEquals("cancelled_before_start", rows[0].phase)
        assertNull(rows[0].errorType)
        assertNull(rows[0].dispatchMs)
        assertNull(rows[0].executionMs)
        assertEquals(400L, rows[0].elapsedMs)
        assertFalse(rows[0].slow)

        assertEquals("cancelled_before_start", rows[1].phase)
        assertEquals(cancellation.javaClass.name, rows[1].errorType)
        assertEquals(2 as Int?, rows[1].batchSize)
        assertNull(rows[1].dispatchMs)
        assertNull(rows[1].executionMs)
        assertEquals(500L, rows[1].elapsedMs)
        assertFalse(rows[1].errorType!!.contains(secret))

        assertEquals("failed", rows[2].phase)
        assertEquals(IllegalStateException::class.java.name, rows[2].errorType)
        assertEquals(200L, rows[2].elapsedMs)

        assertEquals("failed", rows[3].phase)
        assertEquals(startedCancellation.javaClass.name, rows[3].errorType)
        assertEquals(800L, rows[3].dispatchMs)
        assertEquals(200L, rows[3].executionMs)
        assertEquals(1_000L, rows[3].elapsedMs)

        assertEquals("failed", rows[4].phase)
        assertEquals(IllegalStateException::class.java.name, rows[4].errorType)
        assertEquals(100L, rows[4].dispatchMs)
        assertEquals(250L, rows[4].executionMs)
        assertEquals(350L, rows[4].elapsedMs)
        assertEquals(1 as Int?, rows[4].batchSize)

        val text = snapshot.toString()
        assertFalse(text.contains(secret))
        assertFalse(text.contains("Bearer"))
        assertFalse(text.contains("IllegalArgumentException"))
        assertFalse(text.contains("task="))
        assertFalse(text.contains("cause="))
    }

    @Test
    fun retention_preservesActiveTotalWhenOldMetadataIsEvicted() {
        var nanos = 0L
        val diagnostics = ProviderDiagnostics { nanos }
        val traces = mutableListOf<ProviderDiagnosticTrace>()
        repeat(64) { index ->
            traces += diagnostics.dispatch(
                if (index % 2 == 0) PROVIDER_OPERATION_READ else PROVIDER_OPERATION_COMMAND,
                "GET /tasks",
                batchSize = if (index % 2 == 0) null else 4,
            )
        }
        val atCap = diagnostics.snapshot(4, "4.0.0", 33)
        assertEquals(64, atCap.activeCount)
        assertEquals(64, atCap.active.size)
        assertEquals(0, atCap.activeTruncatedCount)
        assertEquals((1L..64L).toList(), atCap.active.map { it.id })
        assertEquals("dispatchers_io", atCap.executionMode)
        assertEquals(10_000L, atCap.slowThresholdMs)
        assertEquals(1, atCap.schemaVersion)

        repeat(6) { offset ->
            traces += diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /tasks", offset)
        }
        val overflow = diagnostics.snapshot(4, "4.0.0", 33)
        assertEquals(70, overflow.activeCount)
        assertEquals(64, overflow.active.size)
        assertEquals(6, overflow.activeTruncatedCount)
        assertEquals(overflow.activeCount - overflow.active.size, overflow.activeTruncatedCount)
        assertEquals((7L..70L).toList(), overflow.active.map { it.id })
        assertTrue(overflow.recent.isEmpty())

        nanos = millis(8_000)
        diagnostics.begin(traces[0])
        nanos = millis(19_000)
        diagnostics.complete(traces[0])
        val evictedDone = diagnostics.snapshot(4, "4.0.0", 33)
        assertEquals(69, evictedDone.activeCount)
        assertEquals(5, evictedDone.activeTruncatedCount)
        assertEquals((7L..70L).toList(), evictedDone.active.map { it.id })
        val evicted = evictedDone.recent.single()
        assertEquals(1L, evicted.id)
        assertEquals("completed", evicted.phase)
        assertEquals("read", evicted.operation)
        assertEquals(8_000L, evicted.dispatchMs)
        assertEquals(11_000L, evicted.executionMs)
        assertEquals(19_000L, evicted.elapsedMs)
        assertTrue(evicted.slow)

        diagnostics.complete(traces[6])
        val listedDone = diagnostics.snapshot(4, "4.0.0", 33)
        assertEquals(68, listedDone.activeCount)
        assertEquals(5, listedDone.activeTruncatedCount)
        assertEquals((8L..70L).toList(), listedDone.active.map { it.id })
        assertEquals(listOf(1L, 7L), listedDone.recent.map { it.id })
        assertEquals("cancelled_before_start", listedDone.recent.last().phase)
        assertNull(listedDone.recent.last().dispatchMs)
        assertNull(listedDone.recent.last().executionMs)
        assertEquals(19_000L, listedDone.recent.last().elapsedMs)

        traces.filter { it.id != 1L && it.id != 7L }.forEach { diagnostics.complete(it) }
        val done = diagnostics.snapshot(4, "4.0.0", 33)
        assertEquals(0, done.activeCount)
        assertEquals(0, done.activeTruncatedCount)
        assertTrue(done.active.isEmpty())
        assertEquals(64, done.recent.size)
        assertEquals(listOf(6L) + (8L..70L).toList(), done.recent.map { it.id })
    }

    @Test
    fun repeatedBeginAndComplete_keepTheFirstOutcome() {
        var nanos = 0L
        val diagnostics = ProviderDiagnostics { nanos }
        val trace = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /history")
        nanos = millis(2_000)
        diagnostics.begin(trace)
        nanos = millis(9_000)
        diagnostics.begin(trace)
        nanos = millis(11_000)
        diagnostics.complete(trace)
        nanos = millis(20_000)
        diagnostics.complete(trace, IllegalStateException("account=secret"))

        val row = diagnostics.snapshot(1, "1", 1).recent.single()
        assertEquals("completed", row.phase)
        assertNull(row.errorType)
        assertEquals(2_000L, row.dispatchMs)
        assertEquals(9_000L, row.executionMs)
        assertEquals(11_000L, row.elapsedMs)
        assertTrue(row.slow)
        assertEquals(0, diagnostics.snapshot(1, "1", 1).activeCount)
        assertFalse(diagnostics.snapshot(1, "1", 1).toString().contains("secret"))

        val late = diagnostics.dispatch(PROVIDER_OPERATION_COMMAND, "POST /api/contentprovider", 1)
        diagnostics.complete(late, CancellationException("secret-token"))
        diagnostics.begin(late)
        val lateRow = diagnostics.snapshot(1, "1", 1).recent.last()
        assertEquals("cancelled_before_start", lateRow.phase)
        assertEquals(CancellationException::class.java.name, lateRow.errorType)
        assertEquals(0, diagnostics.snapshot(1, "1", 1).activeCount)
        assertFalse(lateRow.toString().contains("secret-token"))
    }

    @Test
    fun privateInputs_neverEnterTheSnapshot() {
        val secret = "raw-token-value"
        val taskText = "买菜"
        val diagnostics = ProviderDiagnostics { millis(5_000) }
        val rawRoute = "lifeup://api/reward?token=$secret&content=$taskText"
        val trace = diagnostics.dispatch("read?token=$secret", rawRoute, batchSize = 3)
        diagnostics.begin(trace)
        diagnostics.complete(
            trace,
            IllegalStateException(
                "account=$secret task=$taskText",
                IllegalArgumentException("header Authorization=$secret"),
            ),
        )
        val safe = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /tasks")
        val quoted = diagnostics.dispatch(PROVIDER_OPERATION_COMMAND, "GET /tasks\nid=$secret")

        val snapshot = diagnostics.snapshot(3, "3.0.1", 34)
        val failed = snapshot.recent.single()
        assertEquals(sanitizeProviderRoute("token"), failed.route)
        assertEquals(sanitizeProviderRoute("token"), failed.operation)
        assertEquals(IllegalStateException::class.java.name, failed.errorType)
        assertEquals(3 as Int?, failed.batchSize)
        assertEquals("failed", failed.phase)
        assertEquals("GET /tasks", snapshot.active.single { it.id == safe.id }.route)
        assertEquals("read", snapshot.active.single { it.id == safe.id }.operation)
        assertEquals(
            sanitizeProviderRoute("\n"),
            snapshot.active.single { it.id == quoted.id }.route,
        )
        assertEquals("command", snapshot.active.single { it.id == quoted.id }.operation)

        val text = snapshot.toString() + trace.toString()
        assertFalse(text.contains(secret))
        assertFalse(text.contains(taskText))
        assertFalse(text.contains("Authorization"))
        assertFalse(text.contains("IllegalArgumentException"))
        assertFalse(text.contains("account="))
        assertFalse(text.contains("lifeup://"))
        assertEquals("ProviderDiagnosticTrace(id=${trace.id})", trace.toString())
    }

    @Test
    fun snapshot_keepsSchemaWhenDefaultsAreOmitted() {
        val snapshot = ProviderDiagnostics { 0L }.snapshot(
            cloudVersion = 8,
            cloudVersionName = "8.1.0",
            androidApi = 35,
        )
        assertEquals(1, snapshot.schemaVersion)
        assertEquals("dispatchers_io", snapshot.executionMode)
        assertEquals(10_000L, snapshot.slowThresholdMs)
        assertEquals(0, snapshot.activeCount)
        assertTrue(snapshot.active.isEmpty())
        assertTrue(snapshot.recent.isEmpty())

        val strict = Json { encodeDefaults = false }
        val encoded = strict.encodeToString(snapshot)
        assertTrue(encoded.contains("\"schemaVersion\":1"))
        assertTrue(encoded.contains("\"executionMode\":\"dispatchers_io\""))
        assertTrue(encoded.contains("\"slowThresholdMs\":10000"))
        assertTrue(encoded.contains("\"cloudVersion\":8"))
        assertTrue(encoded.contains("\"cloudVersionName\":\"8.1.0\""))
        assertTrue(encoded.contains("\"androidApi\":35"))
        assertTrue(encoded.contains("\"activeCount\":0"))
        assertTrue(encoded.contains("\"activeTruncatedCount\":0"))
    }

    @Test(timeout = 20_000)
    fun concurrentInFlight_snapshotSeesExecutingWithoutPrivateText() {
        val diagnostics = ProviderDiagnostics()
        val threads = 6
        val entered = CountDownLatch(threads)
        val release = CountDownLatch(1)
        val errors = java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
        val pool = Executors.newFixedThreadPool(threads)
        repeat(threads) {
            pool.execute {
                var announced = false
                try {
                    val trace = diagnostics.dispatch(
                        PROVIDER_OPERATION_READ,
                        "GET /tasks?token=SUPERSECRET",
                    )
                    diagnostics.begin(trace)
                    announced = true
                    entered.countDown()
                    assertTrue(release.await(5, TimeUnit.SECONDS))
                    diagnostics.complete(trace)
                } catch (error: Throwable) {
                    errors.add(error)
                } finally {
                    if (!announced) entered.countDown()
                }
            }
        }
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            val mid = diagnostics.snapshot(9, "9.0.0", 34)
            assertEquals(threads, mid.activeCount)
            assertEquals(threads, mid.active.size)
            assertEquals(0, mid.activeTruncatedCount)
            assertTrue(mid.active.all { it.phase == "executing" })
            assertTrue(mid.active.all { it.route == sanitizeProviderRoute("token") })
            assertFalse(mid.toString().contains("SUPERSECRET"))
            release.countDown()
        } finally {
            release.countDown()
            pool.shutdown()
        }
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS))
        assertTrue(errors.toString(), errors.isEmpty())
        val done = diagnostics.snapshot(9, "9.0.0", 34)
        assertEquals(0, done.activeCount)
        assertEquals(threads, done.recent.size)
        assertTrue(done.recent.all { it.phase == "completed" })
        assertFalse(done.toString().contains("SUPERSECRET"))
    }

    @Test(timeout = 20_000)
    fun concurrentCompletion_staysInsideCaps() {
        val diagnostics = ProviderDiagnostics()
        val threads = 8
        val perThread = 40
        val errors = java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val finished = CountDownLatch(threads)
        repeat(threads) {
            pool.execute {
                try {
                    start.await()
                    repeat(perThread) { step ->
                        val trace = diagnostics.dispatch(
                            if (step % 2 == 0) PROVIDER_OPERATION_READ else PROVIDER_OPERATION_COMMAND,
                            "lifeup://task?token=SUPERSECRET",
                            batchSize = if (step % 3 == 0) 2 else null,
                        )
                        if (step % 7 == 0) {
                            diagnostics.complete(trace, CancellationException("SUPERSECRET"))
                        } else {
                            diagnostics.begin(trace)
                            val error = if (step % 5 == 0) {
                                IllegalStateException("SUPERSECRET account")
                            } else {
                                null
                            }
                            diagnostics.complete(trace, error)
                        }
                        if (step % 11 == 0) {
                            assertConsistent(diagnostics.snapshot(1, "1.0.0", 34))
                        }
                    }
                } catch (error: Throwable) {
                    errors.add(error)
                } finally {
                    finished.countDown()
                }
            }
        }
        start.countDown()
        assertTrue(finished.await(10, TimeUnit.SECONDS))
        pool.shutdown()
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS))
        assertTrue(errors.toString(), errors.isEmpty())
        val done = diagnostics.snapshot(3, "3.0.1", 34)
        assertConsistent(done)
        assertEquals(0, done.activeCount)
        assertEquals(0, done.activeTruncatedCount)
        assertEquals(64, done.recent.size)
        assertTrue(done.recent.all { it.route == sanitizeProviderRoute("token") })
        assertTrue(done.recent.all { it.operation == "read" || it.operation == "command" })
        assertTrue(
            done.recent.all { record ->
                val type = record.errorType
                type == null ||
                    type == IllegalStateException::class.java.name ||
                    type == CancellationException::class.java.name
            },
        )
        assertFalse(done.toString().contains("SUPERSECRET"))
    }

    private fun assertConsistent(snapshot: ProviderDiagnosticsSnapshot) {
        assertTrue(snapshot.active.size <= DIAGNOSTICS_ACTIVE_LIMIT)
        assertTrue(snapshot.recent.size <= DIAGNOSTICS_RECENT_LIMIT)
        assertTrue(snapshot.activeCount >= snapshot.active.size)
        assertEquals(snapshot.activeCount - snapshot.active.size, snapshot.activeTruncatedCount)
        assertTrue(snapshot.active.all { it.phase == "dispatching" || it.phase == "executing" })
        assertTrue(
            snapshot.recent.all {
                it.phase == "completed" ||
                    it.phase == "failed" ||
                    it.phase == "cancelled_before_start"
            },
        )
        assertFalse(snapshot.toString().contains("SUPERSECRET"))
    }

    private fun millis(value: Long): Long = value * 1_000_000L

    private class DispatchCancellation(message: String) : CancellationException(message)
}
