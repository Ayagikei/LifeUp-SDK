package net.lifeupapp.lifeup.http.service

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.lifeupapp.lifeup.api.exception.CloudBusyException
import net.lifeupapp.lifeup.api.exception.CloudException
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.URL
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class ProviderConcurrencyTest {

    @Test
    fun defaults_matchMeasuredCaps() {
        assertEquals(4, HEAVY_ADMISSION_PERMITS)
        assertEquals(2, COMMAND_ADMISSION_PERMITS)
        assertEquals(20_000L, PROVIDER_ADMISSION_WAIT_MS)
        assertEquals(10003, CloudException.ERROR_CLOUD_BUSY)
        assertEquals(
            "LifeUp Cloud is busy; the request was not started. Retry later.",
            CloudBusyException.MESSAGE,
        )
        assertEquals(9, LIGHT_PROVIDER_ROUTES.size)
    }

    @Test(timeout = 20_000)
    fun noHeader_sixHeavyReadsEnterTogether() = runBlocking {
        val entered = CountDownLatch(6)
        val release = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(heavyPermits = 1, waitTimeoutMs = 50)
        try {
            testApplication {
                installHeavy(harness, entered, release, runs)
                coroutineScope {
                    val jobs = List(6) { index ->
                        launch { assertEquals(HttpStatusCode.OK, client.get("/tasks?probe=$index").status) }
                    }
                    assertTrue("unprotected reads did not all enter", awaitLatch(entered))
                    assertEquals(6, runs.get())
                    release.countDown()
                    withTimeout(5_000) { jobs.forEach { it.join() } }
                }
            }
        } finally {
            release.countDown()
            harness.close()
        }
    }

    @Test(timeout = 20_000)
    fun otherHeaderValue_staysOnTheDirectPath() = runBlocking {
        val entered = CountDownLatch(6)
        val release = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(heavyPermits = 1, waitTimeoutMs = 50)
        try {
            testApplication {
                installHeavy(harness, entered, release, runs)
                coroutineScope {
                    val jobs = listOf("off", "limited ", "0", "LIMIT", "yes", "false").map { value ->
                        launch {
                            val response = client.get("/tasks?probe=$value") {
                                header(CONCURRENCY_LIMIT_HEADER, value)
                            }
                            assertEquals(value, HttpStatusCode.OK, response.status)
                        }
                    }
                    assertTrue(awaitLatch(entered))
                    assertEquals(6, runs.get())
                    release.countDown()
                    withTimeout(5_000) { jobs.forEach { it.join() } }
                }
            }
        } finally {
            release.countDown()
            harness.close()
        }
    }

    @Test(timeout = 25_000)
    fun limited_fifthHeavyWaitsWhileLightAndCommandsProceed() = runBlocking {
        val heavyEntered = CountDownLatch(4)
        val fifthEntered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val heavyRuns = AtomicInteger()
        val lightRuns = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 5_000)
        try {
            testApplication {
                application {
                    install(ContentNegotiation) { json(CloudJson) }
                    routing {
                        installLights(harness) { lightRuns.incrementAndGet() }
                        get("/tasks") {
                            val probe = call.request.queryParameters["probe"]
                            call.respondProviderResult(
                                route = "GET /tasks",
                                block = {
                                    heavyRuns.incrementAndGet()
                                    if (probe == "5") fifthEntered.countDown() else heavyEntered.countDown()
                                    awaitIgnoringInterrupt(release)
                                    Result.success(probe.orEmpty())
                                },
                                execution = harness.execution,
                                admission = harness.admission,
                            ) { value -> call.respond(value.wrapAsResponse()) }
                        }
                        post<RawQueryVo>("/api/contentprovider") { body ->
                            call.respondContentProviderPost(
                                body,
                                harness.execution,
                                admission = harness.admission,
                            ) { url ->
                                JsonObject(mapOf("url" to JsonPrimitive(url)))
                            }
                        }
                    }
                }
                coroutineScope {
                    val holders = List(4) { index ->
                        launch { assertEquals(HttpStatusCode.OK, limitedGet("/tasks?probe=${index + 1}").status) }
                    }
                    assertTrue(awaitLatch(heavyEntered))
                    val fifth = launch {
                        assertEquals(HttpStatusCode.OK, limitedGet("/tasks?probe=5").status)
                    }
                    assertTrue(
                        "5th heavy read was not queued",
                        spinUntil { harness.admission.waitersFor("GET /tasks?probe=5") == 1 },
                    )
                    assertEquals("fifth entered while four heavy reads held the pool", 1L, fifthEntered.count)
                    assertEquals(4, heavyRuns.get())
                    assertEquals(0, harness.admission.heavyAvailablePermits())

                    LIGHT_PROVIDER_ROUTES.forEach { template ->
                        val response = limitedGet(lightPath(template))
                        assertEquals(template, HttpStatusCode.OK, response.status)
                        assertTrue(response.bodyAsText().contains(template))
                    }
                    assertEquals(LIGHT_PROVIDER_ROUTES.size, lightRuns.get())
                    assertEquals(1L, fifthEntered.count)

                    val command = client.post("/api/contentprovider") {
                        header(CONCURRENCY_LIMIT_HEADER, "LiMiTeD")
                        contentType(ContentType.Application.Json)
                        setBody("""{"urls":["while-heavy"]}""")
                    }
                    assertEquals(HttpStatusCode.OK, command.status)
                    assertTrue(command.bodyAsText().contains("while-heavy"))
                    assertEquals(1L, fifthEntered.count)

                    release.countDown()
                    assertTrue(awaitLatch(fifthEntered))
                    withTimeout(5_000) { (holders + fifth).forEach { it.join() } }
                    assertEquals(5, heavyRuns.get())
                    val leaders = harness.diagnostics.snapshot(1, "1", 1).recent.filter {
                        it.route == "GET /tasks" && it.phase == "completed" && !it.shared
                    }
                    assertEquals(5, leaders.size)
                    assertTrue(leaders.all { it.limited && it.queueMs != null && it.executionMs != null })
                }
            }
        } finally {
            release.countDown()
            harness.close()
        }
    }

    @Test(timeout = 20_000)
    fun limitedCommands_thirdWaitsAndIdenticalBatchesDoNotCoalesce() = runBlocking {
        val firstEntered = CountDownLatch(2)
        val thirdEntered = CountDownLatch(1)
        val releases = Array(3) { CountDownLatch(1) }
        val calls = Collections.synchronizedList(mutableListOf<String>())
        val slots = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 5_000)
        try {
            testApplication {
                application {
                    install(ContentNegotiation) { json(CloudJson) }
                    routing {
                        post<RawQueryVo>("/api/contentprovider") { body ->
                            call.respondContentProviderPost(
                                body,
                                harness.execution,
                                admission = harness.admission,
                            ) { url ->
                                if (url == "same-a") {
                                    val slot = slots.getAndIncrement()
                                    calls.add("a$slot")
                                    if (slot < 2) firstEntered.countDown() else thirdEntered.countDown()
                                    awaitIgnoringInterrupt(releases[slot])
                                    calls.add("b$slot")
                                } else {
                                    calls.add(url)
                                }
                                JsonObject(mapOf("url" to JsonPrimitive(url)))
                            }
                        }
                    }
                }
                val ordered = postBatch("""{"urls":["z","a"]}""")
                assertEquals(HttpStatusCode.OK, ordered.status)
                assertEquals(listOf("z", "a"), urlsOf(ordered.bodyAsText()))
                calls.clear()

                coroutineScope {
                    val body = """{"urls":["same-a","same-b"]}"""
                    val jobs = List(2) { launch { assertEquals(HttpStatusCode.OK, postBatch(body).status) } }
                    assertTrue(awaitLatch(firstEntered))
                    assertEquals(setOf("a0", "a1"), calls.toSet())
                    assertEquals(0, harness.admission.commandAvailablePermits())
                    val third = launch { assertEquals(HttpStatusCode.OK, postBatch(body).status) }
                    assertFalse("3rd command entered before a permit was free", awaitLatch(thirdEntered, 300))
                    assertFalse(calls.contains("a2"))
                    releases[0].countDown()
                    assertTrue(awaitLatch(thirdEntered))
                    assertTrue(calls.indexOf("a0") < calls.indexOf("b0"))
                    assertTrue(calls.indexOf("b0") < calls.indexOf("a2"))
                    releases[1].countDown()
                    releases[2].countDown()
                    withTimeout(5_000) { (jobs + third).forEach { it.join() } }
                    assertEquals(listOf("b0", "b1", "b2").toSet(), calls.filter { it.startsWith("b") }.toSet())
                    assertEquals(2, harness.admission.commandAvailablePermits())
                }
            }
        } finally {
            releases.forEach { it.countDown() }
            harness.close()
        }
    }

    @Test(timeout = 20_000)
    fun busyTimeout_returns503AndDoesNotRun() = runBlocking {
        val entered = CountDownLatch(4)
        val release = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 2_000)
        val responses = Collections.synchronizedList(mutableListOf<Pair<Int, String>>())
        try {
            testApplication {
                installHeavy(harness, entered, release, runs)
                coroutineScope {
                    val holders = List(4) { index ->
                        launch { assertEquals(HttpStatusCode.OK, limitedGet("/tasks?hold=$index").status) }
                    }
                    assertTrue(awaitLatch(entered))
                    val rejected = List(3) {
                        launch {
                            val response = limitedGet("/tasks?busy=1")
                            responses.add(response.status.value to response.bodyAsText())
                        }
                    }
                    assertTrue(
                        "identical busy reads did not share one flight",
                        spinUntil(800) { harness.admission.waitersFor("GET /tasks?busy=1") == 3 },
                    )
                    withTimeout(5_000) { rejected.forEach { it.join() } }
                    assertEquals(4, runs.get())
                    assertEquals(3, responses.size)
                    responses.forEach { (status, body) ->
                        assertEquals(503, status)
                        val json = CloudJson.parseToJsonElement(body).jsonObject
                        assertEquals(CloudException.ERROR_CLOUD_BUSY, json.getValue("code").jsonPrimitive.content.toInt())
                        assertEquals(CloudBusyException.MESSAGE, json.getValue("message").jsonPrimitive.content)
                        assertTrue(json.getValue("data") is JsonNull)
                    }
                    val busyRows = harness.diagnostics.snapshot(1, "1", 1).recent.filter {
                        it.errorType == CloudBusyException::class.java.name
                    }
                    assertEquals(3, busyRows.size)
                    assertTrue(busyRows.all {
                        it.phase == "cancelled_before_start" && it.limited && it.executionMs == null
                    })
                    assertEquals(1, busyRows.count { !it.shared })
                    assertEquals(2, busyRows.count { it.shared && it.queueMs == null })
                    release.countDown()
                    withTimeout(5_000) { holders.forEach { it.join() } }
                    assertEquals(HttpStatusCode.OK, limitedGet("/tasks?hold=after").status)
                    assertEquals(5, runs.get())
                }
            }
        } finally {
            release.countDown()
            harness.close()
        }
    }

    @Test(timeout = 20_000)
    fun queuedWaiterCancel_doesNotRunOrLeakPermit() = runBlocking {
        val harness = Harness(waitTimeoutMs = 5_000)
        val release = CountDownLatch(1)
        val entered = CountDownLatch(4)
        val runs = AtomicInteger()
        val commandRelease = CountDownLatch(1)
        val commandEntered = CountDownLatch(2)
        val commandRuns = AtomicInteger()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val holders = List(4) { index ->
                scope.launch {
                    harness.admission.read("GET /tasks?i=$index", "GET /tasks", harness.execution) {
                        runs.incrementAndGet()
                        entered.countDown()
                        awaitIgnoringInterrupt(release)
                        "held"
                    }
                }
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            val fifthRuns = AtomicInteger()
            val fifth = scope.launch {
                harness.admission.read("GET /tasks?i=5", "GET /tasks", harness.execution) {
                    fifthRuns.incrementAndGet()
                    "nope"
                }
            }
            assertTrue(spinUntilBlocking { harness.admission.waitersFor("GET /tasks?i=5") == 1 })
            fifth.cancel()
            withTimeout(5_000) { fifth.join() }
            assertEquals(0, fifthRuns.get())
            assertEquals(0, harness.admission.waitersFor("GET /tasks?i=5"))
            assertEquals(0, harness.admission.heavyAvailablePermits())
            release.countDown()
            withTimeout(5_000) { holders.forEach { it.join() } }
            assertEquals(4, runs.get())
            assertEquals(4, harness.admission.heavyAvailablePermits())

            // Cancel before the flight coroutine is scheduled: the map entry must still go.
            val blocked = CountDownLatch(1)
            val reached = CountDownLatch(1)
            val parked = scope.launch(Dispatchers.Default) {
                reached.countDown()
                blocked.await()
            }
            assertTrue(reached.await(5, TimeUnit.SECONDS))
            repeat(200) { index ->
                val early = scope.launch(Dispatchers.Default) {
                    harness.admission.read("GET /tasks?early=$index", "GET /tasks", harness.execution) {
                        "never"
                    }
                }
                early.cancel()
                withTimeout(5_000) { early.join() }
            }
            blocked.countDown()
            withTimeout(5_000) { parked.join() }
            assertEquals(0, harness.admission.waitersFor("GET /tasks?early=0"))
            assertEquals(0, harness.admission.waitersFor("GET /tasks?early=199"))
            assertEquals(4, harness.admission.heavyAvailablePermits())
            val after = harness.admission.read("GET /tasks?early=after", "GET /tasks", harness.execution) { "ok" }
            assertEquals("ok", after)

            val commands = List(2) {
                scope.launch {
                    harness.admission.command("POST /api/contentprovider", 2, harness.execution) {
                        commandRuns.incrementAndGet()
                        commandEntered.countDown()
                        awaitIgnoringInterrupt(commandRelease)
                        "cmd"
                    }
                }
            }
            assertTrue(commandEntered.await(5, TimeUnit.SECONDS))
            val queuedCommand = AtomicInteger()
            val third = scope.launch {
                harness.admission.command("POST /api/contentprovider", 2, harness.execution) {
                    queuedCommand.incrementAndGet()
                    "nope"
                }
            }
            assertTrue(spinUntilBlocking { harness.admission.commandAvailablePermits() == 0 && commandRuns.get() == 2 })
            third.cancel()
            withTimeout(5_000) { third.join() }
            assertEquals(0, queuedCommand.get())
            commandRelease.countDown()
            withTimeout(5_000) { commands.forEach { it.join() } }
            assertEquals(2, commandRuns.get())
            assertEquals(2, harness.admission.commandAvailablePermits())
        } finally {
            release.countDown()
            commandRelease.countDown()
            scope.cancel()
            harness.close()
        }
    }

    @Test(timeout = 20_000)
    fun coalescedReads_runOncePerDistinctUri() = runBlocking {
        val firstEntered = CountDownLatch(1)
        val secondEntered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 5_000)
        try {
            testApplication {
                application {
                    install(ContentNegotiation) { json(CloudJson) }
                    routing {
                        get("/tasks") {
                            val probe = call.request.queryParameters["probe"]
                            call.respondProviderResult(
                                route = "GET /tasks",
                                block = {
                                    runs.incrementAndGet()
                                    if (probe == "1") firstEntered.countDown() else secondEntered.countDown()
                                    awaitIgnoringInterrupt(release)
                                    Result.success("body-$probe")
                                },
                                execution = harness.execution,
                                admission = harness.admission,
                            ) { value -> call.respond(value.wrapAsResponse()) }
                        }
                    }
                }
                coroutineScope {
                    val bodies = Collections.synchronizedList(mutableListOf<String>())
                    val firstWave = List(4) {
                        launch { bodies.add(limitedGet("/tasks?probe=1").bodyAsText()) }
                    }
                    assertTrue(awaitLatch(firstEntered))
                    assertTrue(spinUntil {
                        val active = harness.diagnostics.snapshot(1, "1", 1).active
                        active.count { it.shared && it.phase == "dispatching" } == 3 &&
                            active.count { !it.shared && it.phase == "executing" } == 1
                    })
                    assertEquals(1, runs.get())
                    val other = launch { bodies.add(limitedGet("/tasks?probe=2").bodyAsText()) }
                    assertTrue(awaitLatch(secondEntered))
                    assertEquals(2, runs.get())
                    release.countDown()
                    withTimeout(5_000) { (firstWave + other).forEach { it.join() } }
                    val parsed = bodies.map { CloudJson.parseToJsonElement(it).jsonObject.getValue("data").jsonPrimitive.content }
                    assertEquals(4, parsed.count { it == "body-1" })
                    assertEquals(listOf("body-2"), parsed.filter { it == "body-2" })
                    val done = harness.diagnostics.snapshot(1, "1", 1).recent
                    val shared = done.filter { it.shared }
                    assertEquals(3, shared.size)
                    assertTrue(shared.all { it.phase == "completed" && it.executionMs == null && it.queueMs == null && it.limited })
                    assertEquals(2, done.count { !it.shared && it.phase == "completed" && it.executionMs != null })
                }
                val again = limitedGet("/tasks?probe=1")
                assertEquals(HttpStatusCode.OK, again.status)
                assertEquals(3, runs.get())
                assertTrue(again.bodyAsText().contains("body-1"))
            }
        } finally {
            release.countDown()
            harness.close()
        }
    }

    @Test(timeout = 15_000)
    fun leaderCancel_doesNotCancelSharedRead() = runBlocking {
        val release = CountDownLatch(1)
        val entered = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 5_000)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val leader = scope.launch {
                val body: String = harness.admission.read("GET /tasks?same=1", "GET /tasks", harness.execution) {
                    runs.incrementAndGet()
                    entered.countDown()
                    awaitIgnoringInterrupt(release)
                    "shared-body"
                }
                check(body == "shared-body")
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            val followerBody = AtomicReference<String>()
            val follower = scope.launch {
                followerBody.set(
                    harness.admission.read("GET /tasks?same=1", "GET /tasks", harness.execution) {
                        runs.incrementAndGet()
                        "other"
                    },
                )
            }
            assertTrue(spinUntilBlocking { harness.admission.waitersFor("GET /tasks?same=1") == 2 })
            leader.cancel()
            withTimeout(5_000) { leader.join() }
            assertTrue(leader.isCancelled)
            assertEquals(1, runs.get())
            release.countDown()
            withTimeout(5_000) { follower.join() }
            assertEquals("shared-body", followerBody.get())
            assertEquals(1, runs.get())
            val fresh = harness.admission.read("GET /tasks?same=1", "GET /tasks", harness.execution) {
                runs.incrementAndGet()
                "fresh"
            }
            assertEquals("fresh", fresh)
            assertEquals(2, runs.get())
            val recent = harness.diagnostics.snapshot(1, "1", 1).recent
            assertEquals(
                1,
                recent.count { it.shared && it.phase == "completed" && it.executionMs == null },
            )
            assertTrue(recent.any { !it.shared && it.executionMs != null && it.phase == "completed" })
        } finally {
            release.countDown()
            scope.cancel()
            harness.close()
        }
    }

    @Test
    fun cors_allowsConcurrencyHeader() = testApplication {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            install(CORS) { configureCloudCors() }
            routing {
                get("/tasks") {
                    call.respond(net.lifeupapp.lifeup.http.vo.HttpResponse.success("ok"))
                }
            }
        }
        val preflight = client.options("/tasks") {
            header(HttpHeaders.Origin, "http://example.com")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Get.value)
            header(HttpHeaders.AccessControlRequestHeaders, CONCURRENCY_LIMIT_HEADER)
        }
        val allow = preflight.headers[HttpHeaders.AccessControlAllowHeaders].orEmpty()
        assertTrue(allow, allow.contains(CONCURRENCY_LIMIT_HEADER, ignoreCase = true))
    }

    @Test(timeout = 30_000)
    fun nettyAbort_keepsHeavyPermitUntilCallbackReturns() {
        val releases = Array(4) { CountDownLatch(1) }
        val entered = CountDownLatch(4)
        val fifthEntered = CountDownLatch(1)
        val runs = AtomicInteger()
        val order = Collections.synchronizedList(mutableListOf<String>())
        val harness = Harness(waitTimeoutMs = 5_000)
        val sockets = mutableListOf<Socket>()
        var server: ApplicationEngine? = null
        var fifth: Thread? = null
        try {
            server = startServer(harness) { probe ->
                val index = probe?.toIntOrNull()
                runs.incrementAndGet()
                if (index == 5) {
                    order.add("enter-5")
                    fifthEntered.countDown()
                } else if (index != null && index in 1..4) {
                    entered.countDown()
                    awaitIgnoringInterrupt(releases[index - 1])
                    order.add("done-$index")
                }
                Result.success(probe ?: "")
            }
            val port = boundPort(server)
            repeat(4) { index -> sockets += openLimited(port, "/tasks?probe=${index + 1}") }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            assertEquals(4, runs.get())
            resetConnection(sockets.removeAt(0))
            fifth = thread(name = "fifth-heavy") {
                exchangeLimited(port, "/tasks?probe=5")
            }
            assertTrue(
                "aborted holder released its permit before the callback returned",
                spinUntilBlocking { harness.admission.waitersFor("GET /tasks?probe=5") == 1 },
            )
            assertFalse(order.contains("enter-5"))
            assertEquals(4, runs.get())
            releases[0].countDown()
            assertTrue(fifthEntered.await(5, TimeUnit.SECONDS))
            assertTrue(order.indexOf("done-1") < order.indexOf("enter-5"))
            assertEquals(5, runs.get())
            for (index in 1..3) releases[index].countDown()
            fifth.join(5_000)
            assertEquals(5, runs.get())
        } finally {
            releases.forEach { it.countDown() }
            sockets.forEach(::resetConnection)
            fifth?.join(2_000)
            server?.stop(100, 2_000)
            harness.close()
        }
    }

    @Test(timeout = 30_000)
    fun nettyLeaderDisconnect_doesNotCancelSharedRead() {
        val release = CountDownLatch(1)
        val entered = CountDownLatch(1)
        val runs = AtomicInteger()
        val harness = Harness(waitTimeoutMs = 5_000)
        val sockets = mutableListOf<Socket>()
        var server: ApplicationEngine? = null
        val followerResult = AtomicReference<StatusBody>()
        var follower: Thread? = null
        try {
            server = startServer(harness) {
                runs.incrementAndGet()
                entered.countDown()
                awaitIgnoringInterrupt(release)
                Result.success("shared-body")
            }
            val port = boundPort(server)
            sockets += openLimited(port, "/tasks?same=1")
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            follower = thread(name = "shared-follower") {
                followerResult.set(exchangeLimited(port, "/tasks?same=1"))
            }
            assertTrue(spinUntilBlocking { harness.admission.waitersFor("GET /tasks?same=1") == 2 })
            resetConnection(sockets.removeAt(0))
            assertEquals(1, runs.get())
            assertNull(followerResult.get())
            release.countDown()
            follower.join(8_000)
            val body = followerResult.get()
            assertEquals(body?.body, 200, body?.status)
            assertTrue(body?.body.orEmpty().contains("shared-body"))
            assertEquals(1, runs.get())
        } finally {
            release.countDown()
            sockets.forEach(::resetConnection)
            follower?.join(2_000)
            server?.stop(100, 2_000)
            harness.close()
        }
    }

    private class Harness(
        heavyPermits: Int = HEAVY_ADMISSION_PERMITS,
        commandPermits: Int = COMMAND_ADMISSION_PERMITS,
        waitTimeoutMs: Long = PROVIDER_ADMISSION_WAIT_MS,
    ) {
        val diagnostics = ProviderDiagnostics()
        val execution = ProviderExecution(diagnostics = diagnostics)
        val admission = ProviderAdmission(
            heavyPermits = heavyPermits,
            commandPermits = commandPermits,
            waitTimeoutMs = waitTimeoutMs,
        )

        fun close() {
            admission.close()
        }
    }

    private fun ApplicationTestBuilder.installHeavy(
        harness: Harness,
        entered: CountDownLatch,
        release: CountDownLatch,
        runs: AtomicInteger,
    ) {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            routing {
                get("/tasks") {
                    call.respondProviderResult(
                        route = "GET /tasks",
                        block = {
                            runs.incrementAndGet()
                            entered.countDown()
                            awaitIgnoringInterrupt(release)
                            Result.success("ok")
                        },
                        execution = harness.execution,
                        admission = harness.admission,
                    ) { value -> call.respond(value.wrapAsResponse()) }
                }
            }
        }
    }

    private fun io.ktor.server.routing.Routing.installLights(
        harness: Harness,
        onRun: () -> Unit,
    ) {
        LIGHT_PROVIDER_ROUTES.forEach { template ->
            get(lightPath(template)) {
                call.respondProviderResult(
                    route = template,
                    block = {
                        onRun()
                        Result.success(template)
                    },
                    execution = harness.execution,
                    admission = harness.admission,
                ) { value -> call.respond(value.wrapAsResponse()) }
            }
        }
    }

    private suspend fun ApplicationTestBuilder.limitedGet(path: String) = client.get(path) {
        header(CONCURRENCY_LIMIT_HEADER, "LiMiTeD")
    }

    private suspend fun ApplicationTestBuilder.postBatch(json: String) = client.post("/api/contentprovider") {
        header(CONCURRENCY_LIMIT_HEADER, "limited")
        contentType(ContentType.Application.Json)
        setBody(json)
    }

    private fun lightPath(template: String): String {
        return template.removePrefix("GET ").replace("{id}", "9")
    }

    private fun urlsOf(body: String): List<String> {
        return CloudJson.parseToJsonElement(body).jsonObject.getValue("data").jsonArray.map { row ->
            row.jsonObject.getValue("url").jsonPrimitive.content
        }
    }

    private suspend fun awaitLatch(latch: CountDownLatch, timeoutMs: Long = 5_000): Boolean {
        return withContext(Dispatchers.IO) { latch.await(timeoutMs, TimeUnit.MILLISECONDS) }
    }

    private suspend fun spinUntil(timeoutMs: Long = 2_000, probe: () -> Boolean): Boolean {
        return withContext(Dispatchers.IO) { spinUntilBlocking(timeoutMs, probe) }
    }

    private fun spinUntilBlocking(timeoutMs: Long = 2_000, probe: () -> Boolean): Boolean {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
        while (System.nanoTime() < deadline) {
            if (probe()) return true
            Thread.sleep(10)
        }
        return probe()
    }

    private fun startServer(
        harness: Harness,
        onRead: (String?) -> Result<String>,
    ): ApplicationEngine {
        val server = embeddedServer(Netty, port = 0, host = "127.0.0.1", watchPaths = emptyList()) {
            install(ContentNegotiation) { json(CloudJson) }
            routing {
                get("/tasks") {
                    val probe = call.request.queryParameters["probe"] ?: call.request.queryParameters["same"]
                    call.respondProviderResult(
                        route = "GET /tasks",
                        block = { onRead(probe) },
                        execution = harness.execution,
                        admission = harness.admission,
                    ) { value -> call.respond(value.wrapAsResponse()) }
                }
            }
        }
        server.start(wait = false)
        return server
    }

    private fun boundPort(server: ApplicationEngine): Int {
        val port = runBlocking { server.resolvedConnectors().firstOrNull()?.port ?: 0 }
        assertTrue("Netty did not bind an ephemeral localhost port", port > 0)
        return port
    }

    private fun openLimited(port: Int, path: String): Socket {
        val socket = Socket()
        socket.tcpNoDelay = true
        socket.setSoLinger(true, 0)
        socket.connect(InetSocketAddress("127.0.0.1", port), 3_000)
        val header = buildString {
            append("GET ").append(path).append(" HTTP/1.1\r\n")
            append("Host: 127.0.0.1:").append(port).append("\r\n")
            append(CONCURRENCY_LIMIT_HEADER).append(": limited\r\n")
            append("Accept: application/json\r\n")
            append("Connection: close\r\n\r\n")
        }
        socket.getOutputStream().apply {
            write(header.toByteArray(Charsets.US_ASCII))
            flush()
        }
        return socket
    }

    private fun exchangeLimited(port: Int, path: String): StatusBody {
        val connection = (URI("http://127.0.0.1:$port$path").toURL() as URL).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 3_000
            connection.readTimeout = 8_000
            connection.setRequestProperty(CONCURRENCY_LIMIT_HEADER, "limited")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Connection", "close")
            val status = connection.responseCode
            val stream = if (status >= 400) connection.errorStream else connection.inputStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            StatusBody(status, body)
        } finally {
            connection.disconnect()
        }
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

    private data class StatusBody(val status: Int, val body: String)
}

private fun awaitIgnoringInterrupt(latch: CountDownLatch) {
    while (latch.count > 0) {
        try {
            if (latch.await(200, TimeUnit.MILLISECONDS)) return
        } catch (_: InterruptedException) {
            Thread.interrupted()
        }
    }
}
