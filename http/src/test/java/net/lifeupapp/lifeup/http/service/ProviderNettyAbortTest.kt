package net.lifeupapp.lifeup.http.service

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.URL
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

/**
 * Host-only TCP check. Real Netty on an ephemeral localhost port, not Android and not Binder.
 * Blocking provider work is a latch. Clients are reset with SO_LINGER 0 after the callback has entered.
 */
class ProviderNettyAbortTest {

    @Test(timeout = 20_000)
    fun abortedReads_continueBeyondOldCapWithoutBusyOrDuplicate() {
        val release = CountDownLatch(1)
        val entered = CountDownLatch(READS_BEYOND_OLD_CAP)
        val finished = CountDownLatch(READS_BEYOND_OLD_CAP)
        val holders = AtomicInteger()
        val extra = AtomicInteger()
        val fresh = AtomicInteger()
        val interrupted = AtomicBoolean(false)
        val callbackThreads = Collections.synchronizedSet(mutableSetOf<String>())
        val commandUrls = Collections.synchronizedList(mutableListOf<String>())
        val sockets = mutableListOf<Socket>()
        val testThread = Thread.currentThread().name
        val threadsBefore = nettyThreadNames()
        var server: ApplicationEngine? = null
        try {
            server = startCloud(
                onRead = { probe ->
                    callbackThreads.add(Thread.currentThread().name)
                    when (probe) {
                        "extra" -> {
                            extra.incrementAndGet()
                            Result.success("extra")
                        }
                        "fresh" -> {
                            fresh.incrementAndGet()
                            Result.success("fresh")
                        }
                        else -> {
                            holders.incrementAndGet()
                            entered.countDown()
                            if (awaitAndRecordInterrupt(release)) interrupted.set(true)
                            Result.success("holder")
                        }
                    }
                },
                onCommand = { url ->
                    commandUrls.add(url)
                    JsonObject(mapOf("url" to JsonPrimitive(url)))
                },
                onReadExit = { probe ->
                    if (probe == null) finished.countDown()
                },
            )
            val port = boundPort(server)
            repeat(READS_BEYOND_OLD_CAP) {
                sockets += openRequest(port, "GET", "/info", null)
            }
            assertTrue(
                "Netty did not enter $READS_BEYOND_OLD_CAP read callbacks (entered=${holders.get()})",
                entered.await(5, TimeUnit.SECONDS),
            )
            assertEquals(READS_BEYOND_OLD_CAP, holders.get())
            sockets.forEach(::resetConnection)
            sockets.clear()
            assertActiveDiagnostics(port, READS_BEYOND_OLD_CAP, "read")

            val root = exchange(port, "GET", "/")
            assertEquals("Cloud root did not respond while reads were blocked: ${root.body}", 200, root.status)
            assertTrue(root.body.contains("LifeUp Cloud"))

            val otherLane = exchange(port, "POST", "/api/contentprovider", """{"urls":["cross-check"]}""")
            assertNotBusy(otherLane, "command while $READS_BEYOND_OLD_CAP reads were blocked")
            assertEquals(listOf("cross-check"), urlsOf(otherLane.body))

            val additional = exchange(port, "GET", "/info?probe=extra")
            assertNotBusy(additional, "additional read while disconnected reads were still inside the callback")
            assertEquals("extra", jsonObject(additional.body).getValue("data").jsonPrimitive.content)
            assertEquals(1, extra.get())
            assertEquals(READS_BEYOND_OLD_CAP, holders.get())
            assertFalse(interrupted.get())
            assertOffNettyThread(callbackThreads, testThread)

            release.countDown()
            assertTrue(
                "aborted read callbacks did not finish after release",
                finished.await(5, TimeUnit.SECONDS),
            )
            assertActiveDiagnostics(port, 0)
            assertEquals(READS_BEYOND_OLD_CAP, holders.get())
            assertEquals(1, extra.get())
            assertFalse(interrupted.get())

            val again = exchange(port, "GET", "/info?probe=fresh")
            assertNotBusy(again, "fresh read after the disconnected callbacks finished")
            assertEquals("fresh", jsonObject(again.body).getValue("data").jsonPrimitive.content)
            assertEquals(1, fresh.get())
            assertEquals(READS_BEYOND_OLD_CAP, holders.get())
            assertEquals(listOf("cross-check"), synchronized(commandUrls) { commandUrls.toList() })
        } finally {
            release.countDown()
            sockets.forEach(::resetConnection)
            server?.let { stopServer(it) }
            assertNettyStopped(threadsBefore)
        }
    }

    @Test(timeout = 20_000)
    fun abortedCommandBatches_continueBeyondOldCapWithoutBusyOrDuplicate() {
        val releaseFirst = CountDownLatch(1)
        val releaseSecond = CountDownLatch(1)
        val firstEntered = CountDownLatch(COMMANDS_BEYOND_OLD_CAP)
        val secondEntered = CountDownLatch(COMMANDS_BEYOND_OLD_CAP)
        val finished = CountDownLatch(COMMANDS_BEYOND_OLD_CAP)
        val interrupted = AtomicBoolean(false)
        val invoked = Collections.synchronizedList(mutableListOf<String>())
        val callbackThreads = Collections.synchronizedSet(mutableSetOf<String>())
        val reads = AtomicInteger()
        val sockets = mutableListOf<Socket>()
        val testThread = Thread.currentThread().name
        val threadsBefore = nettyThreadNames()
        var server: ApplicationEngine? = null
        val batchNames = listOf("batchA", "batchB", "batchC")
        try {
            server = startCloud(
                onRead = {
                    reads.incrementAndGet()
                    Result.success("read")
                },
                onCommand = { url ->
                    callbackThreads.add(Thread.currentThread().name)
                    invoked.add(url)
                    if (url.endsWith("-1") && url.startsWith("batch")) {
                        firstEntered.countDown()
                        if (awaitAndRecordInterrupt(releaseFirst)) interrupted.set(true)
                    } else if (url.endsWith("-2") && url.startsWith("batch")) {
                        secondEntered.countDown()
                        if (awaitAndRecordInterrupt(releaseSecond)) interrupted.set(true)
                    }
                    JsonObject(mapOf("step" to JsonPrimitive(url)))
                },
                onCommandExit = { body ->
                    if (body.urls?.firstOrNull() in batchNames.map { "$it-1" }) {
                        finished.countDown()
                    }
                },
            )
            val port = boundPort(server)
            batchNames.forEach { name ->
                sockets += openRequest(
                    port,
                    "POST",
                    "/api/contentprovider",
                    """{"urls":["$name-1","$name-2"]}""",
                )
            }
            assertTrue(
                "Netty did not enter $COMMANDS_BEYOND_OLD_CAP command batches (invoked=${snapshot(invoked)})",
                firstEntered.await(5, TimeUnit.SECONDS),
            )
            assertEquals(batchNames.map { "$it-1" }.toSet(), snapshot(invoked).toSet())
            sockets.forEach(::resetConnection)
            sockets.clear()
            assertActiveDiagnostics(port, COMMANDS_BEYOND_OLD_CAP, "command", 2)

            val root = exchange(port, "GET", "/")
            assertEquals("Cloud root did not respond while commands were blocked: ${root.body}", 200, root.status)
            assertTrue(root.body.contains("LifeUp Cloud"))

            val otherRead = exchange(port, "GET", "/info")
            assertNotBusy(otherRead, "read while $COMMANDS_BEYOND_OLD_CAP command batches were blocked")
            assertEquals("read", jsonObject(otherRead.body).getValue("data").jsonPrimitive.content)
            assertEquals(1, reads.get())

            val additional = exchange(
                port,
                "POST",
                "/api/contentprovider",
                """{"urls":["extra-1"]}""",
            )
            assertNotBusy(additional, "additional command while disconnected batches were still on the first URL")
            assertEquals(listOf("extra-1"), urlsOf(additional.body))
            assertEquals(1, snapshot(invoked).count { it == "extra-1" })
            assertTrue(snapshot(invoked).none { it.endsWith("-2") })
            assertFalse(interrupted.get())
            assertOffNettyThread(callbackThreads, testThread)

            releaseFirst.countDown()
            assertTrue(
                "batch did not reach the second URL after the first step (invoked=${snapshot(invoked)})",
                secondEntered.await(5, TimeUnit.SECONDS),
            )
            val midBatch = snapshot(invoked)
            batchNames.forEach { name ->
                assertTrue(midBatch.indexOf("$name-1") < midBatch.indexOf("$name-2"))
                assertEquals(1, midBatch.count { it == "$name-1" })
                assertEquals(1, midBatch.count { it == "$name-2" })
            }
            assertEquals(1, midBatch.count { it == "extra-1" })
            assertActiveDiagnostics(port, COMMANDS_BEYOND_OLD_CAP, "command", 2)

            releaseSecond.countDown()
            assertTrue(
                "aborted command batches did not finish the second URL",
                finished.await(5, TimeUnit.SECONDS),
            )
            assertActiveDiagnostics(port, 0)
            assertFalse(interrupted.get())
            assertEquals(batchNames.size * 2 + 1, snapshot(invoked).size)

            val again = exchange(
                port,
                "POST",
                "/api/contentprovider",
                """{"urls":["fresh-1"],"url":"fresh-tail"}""",
            )
            assertNotBusy(again, "fresh command after the disconnected batches finished")
            assertEquals(listOf("fresh-1", "fresh-tail"), urlsOf(again.body))
            assertEquals(midBatch + listOf("fresh-1", "fresh-tail"), snapshot(invoked))
            assertEquals(1, reads.get())
        } finally {
            releaseFirst.countDown()
            releaseSecond.countDown()
            sockets.forEach(::resetConnection)
            server?.let { stopServer(it) }
            assertNettyStopped(threadsBefore)
        }
    }

    private fun assertActiveDiagnostics(port: Int, count: Int, operation: String? = null, batchSize: Int? = null) {
        val response = exchange(port, "GET", "/diagnostics")
        assertEquals("Diagnostics must respond without the blocked Provider: ${response.body}", 200, response.status)
        val data = jsonObject(response.body).getValue("data").jsonObject
        assertEquals(count, data.getValue("activeCount").jsonPrimitive.content.toInt())
        assertEquals(0, data.getValue("activeTruncatedCount").jsonPrimitive.content.toInt())
        val active = data.getValue("active").jsonArray
        assertEquals(count, active.size)
        active.forEach { entry ->
            val record = entry.jsonObject
            assertEquals("executing", record.getValue("phase").jsonPrimitive.content)
            assertEquals(operation, record.getValue("operation").jsonPrimitive.content)
            if (batchSize != null) {
                assertEquals(batchSize, record.getValue("batchSize").jsonPrimitive.content.toInt())
            }
        }
    }

    private fun startCloud(
        onRead: (String?) -> Result<String>,
        onCommand: (String) -> JsonObject?,
        onReadExit: (String?) -> Unit = {},
        onCommandExit: (RawQueryVo) -> Unit = {},
    ): ApplicationEngine {
        val diagnostics = ProviderDiagnostics()
        val execution = ProviderExecution(diagnostics = diagnostics)
        val server = embeddedServer(
            Netty,
            port = 0,
            host = "127.0.0.1",
            watchPaths = emptyList(),
        ) {
            install(ContentNegotiation) { json(CloudJson) }
            routing {
                registerDiagnosticsRoute(diagnostics, 30004, "3.0.4-test", 34)
                get("/") {
                    call.respondText("Hello from LifeUp Cloud!")
                }
                get("/info") {
                    val probe = call.request.queryParameters["probe"]
                    try {
                        call.respondProviderResult(
                            route = "GET /info",
                            block = { onRead(probe) },
                            execution = execution,
                        ) { value ->
                            call.respond(value.wrapAsResponse())
                        }
                    } finally {
                        onReadExit(probe)
                    }
                }
                post<RawQueryVo>("/api/contentprovider") { body ->
                    try {
                        call.respondContentProviderPost(body, execution, onCommand)
                    } finally {
                        onCommandExit(body)
                    }
                }
            }
        }
        server.start(wait = false)
        return server
    }

    private fun boundPort(server: ApplicationEngine): Int {
        val port = kotlinx.coroutines.runBlocking {
            server.resolvedConnectors().firstOrNull()?.port ?: 0
        }
        assertTrue("Netty did not bind an ephemeral localhost port", port > 0)
        return port
    }

    private fun openRequest(port: Int, method: String, path: String, jsonBody: String?): Socket {
        val socket = Socket()
        socket.tcpNoDelay = true
        socket.setSoLinger(true, 0)
        socket.connect(InetSocketAddress("127.0.0.1", port), 3_000)
        val bodyBytes = jsonBody?.toByteArray(Charsets.UTF_8)
        val header = buildString {
            append(method).append(' ').append(path).append(" HTTP/1.1\r\n")
            append("Host: 127.0.0.1:").append(port).append("\r\n")
            append("Accept: application/json\r\n")
            append("Connection: close\r\n")
            if (bodyBytes != null) {
                append("Content-Type: application/json\r\n")
                append("Content-Length: ").append(bodyBytes.size).append("\r\n")
            }
            append("\r\n")
        }
        val out = socket.getOutputStream()
        out.write(header.toByteArray(Charsets.US_ASCII))
        if (bodyBytes != null) out.write(bodyBytes)
        out.flush()
        return socket
    }

    private fun resetConnection(socket: Socket) {
        try {
            socket.setSoLinger(true, 0)
        } catch (_: Exception) {
        }
        try {
            socket.close()
        } catch (_: Exception) {
        }
    }

    private fun exchange(port: Int, method: String, path: String, jsonBody: String? = null): StatusBody {
        val connection = (URI("http://127.0.0.1:$port$path").toURL() as URL).openConnection()
            as java.net.HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 3_000
            connection.readTimeout = 3_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Connection", "close")
            if (jsonBody != null) {
                val bytes = jsonBody.toByteArray(Charsets.UTF_8)
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            val status = connection.responseCode
            val stream = if (status >= 400) connection.errorStream else connection.inputStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            StatusBody(status, body)
        } finally {
            connection.disconnect()
        }
    }

    private fun assertNotBusy(response: StatusBody, situation: String) {
        assertEquals("$situation status body=${response.body}", 200, response.status)
        assertFalse(response.body.contains("LifeUp provider is busy"))
        assertFalse(response.body.contains("\"code\":503"))
    }

    private fun assertOffNettyThread(callbackThreads: Set<String>, testThread: String) {
        assertTrue("provider callback did not record a worker thread", callbackThreads.isNotEmpty())
        assertTrue(
            "provider callback ran on a Netty thread: $callbackThreads",
            callbackThreads.none { isNettyWorker(it) },
        )
        assertTrue(
            "provider callback stayed on the test thread: $callbackThreads",
            callbackThreads.none { it == testThread },
        )
    }

    private fun jsonObject(body: String) = CloudJson.parseToJsonElement(body).jsonObject

    private fun urlsOf(body: String): List<String> {
        return jsonObject(body).getValue("data").jsonArray.map { row ->
            row.jsonObject.getValue("url").jsonPrimitive.content
        }
    }

    private fun snapshot(urls: List<String>): List<String> = synchronized(urls) { urls.toList() }

    private fun awaitAndRecordInterrupt(latch: CountDownLatch): Boolean {
        var interrupted = false
        while (latch.count > 0) {
            try {
                if (latch.await(250, TimeUnit.MILLISECONDS)) break
            } catch (_: InterruptedException) {
                interrupted = true
            }
        }
        return interrupted || Thread.currentThread().isInterrupted
    }

    private fun stopServer(server: ApplicationEngine) {
        server.stop(100, 2_000)
    }

    private fun nettyThreadNames(): Set<String> {
        return Thread.getAllStackTraces().keys
            .filter { it.isAlive && isNettyWorker(it.name) }
            .map { it.name }
            .toSet()
    }

    private fun isNettyWorker(name: String): Boolean {
        val lower = name.lowercase()
        return "eventloop" in lower || "netty" in lower || "ktor-netty" in lower
    }

    private fun assertNettyStopped(before: Set<String>) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        var leftover = nettyThreadNames() - before
        while (leftover.isNotEmpty() && System.nanoTime() < deadline) {
            LockSupport.parkNanos(1_000_000)
            leftover = nettyThreadNames() - before
        }
        assertTrue("Netty worker threads still alive after stop: $leftover", leftover.isEmpty())
    }

    private data class StatusBody(val status: Int, val body: String)

    private companion object {
        const val READS_BEYOND_OLD_CAP = 5
        const val COMMANDS_BEYOND_OLD_CAP = 3
    }
}
