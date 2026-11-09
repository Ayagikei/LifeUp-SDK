package net.lifeupapp.lifeup.http.service

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.lifeupapp.lifeup.api.exception.ContentProviderException
import net.lifeupapp.lifeup.http.vo.HttpResponse
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ProviderRouteExecutionTest {

    @Test(timeout = 15_000)
    fun infoRoute_successIsNotCached() {
        val logs = Collections.synchronizedList(mutableListOf<String>())
        val execution = ProviderExecution(log = { logs.add(it) })
        val runs = AtomicInteger()
        testApplication {
            installProbes(execution, runs)
            val first = client.get("/info")
            val second = client.get("/info")
            assertEquals(HttpStatusCode.OK, first.status)
            assertEquals(HttpStatusCode.OK, second.status)
            assertEquals("1", dataOf(first.bodyAsText()))
            assertEquals("2", dataOf(second.bodyAsText()))
            assertTrue(!first.headers[HttpHeaders.ETag].isNullOrBlank())
            assertTrue(!second.headers[HttpHeaders.ETag].isNullOrBlank())
            assertNotEquals(first.headers[HttpHeaders.ETag], second.headers[HttpHeaders.ETag])
            assertEquals(2, runs.get())
        }
        assertTrue(logs.any {
            it.startsWith("$PROVIDER_LOG_BEGIN ") &&
                it.contains("operation=read") &&
                it.contains("route=\"GET /info\"")
        })
        assertTrue(logs.any { it.startsWith("$PROVIDER_LOG_END ") && it.contains("durationMs=") })
        assertTrue(logs.none {
            "provider_busy" in it ||
                "provider_http_admission" in it ||
                "lane=" in it ||
                "lifeup://" in it ||
                "token=" in it
        })
    }

    @Test(timeout = 20_000)
    fun routes_acceptFiveReadsAndThreeCommandsWithoutBusy() {
        val execution = ProviderExecution()
        val readEntered = CountDownLatch(5)
        val commandEntered = CountDownLatch(3)
        val release = CountDownLatch(1)
        val readRuns = AtomicInteger()
        val commandCalls = Collections.synchronizedList(mutableListOf<String>())
        try {
            testApplication {
                application {
                    install(ContentNegotiation) { json(CloudJson) }
                    installResourceConditionalGet()
                    routing {
                        get("/info") {
                            call.respondProviderResult(
                                "GET /info",
                                execution = execution,
                                block = {
                                    Result.success(readRuns.incrementAndGet()).also {
                                        readEntered.countDown()
                                        awaitIgnoringInterrupt(release)
                                    }
                                },
                            ) { value ->
                                call.respondResource(value.wrapAsResponse())
                            }
                        }
                        post<RawQueryVo>("/api/contentprovider") { body ->
                            call.respondContentProviderPost(body, execution) { url ->
                                commandCalls.add(url)
                                commandEntered.countDown()
                                awaitIgnoringInterrupt(release)
                                JsonObject(mapOf("url" to JsonPrimitive(url)))
                            }
                        }
                    }
                }
                coroutineScope {
                    val jobs = List(5) {
                        launch {
                            val response = client.get("/info")
                            assertEquals(HttpStatusCode.OK, response.status)
                            assertTrue("busy" !in response.bodyAsText())
                        }
                    } + List(3) { index ->
                        launch {
                            val response = client.post("/api/contentprovider") {
                                contentType(ContentType.Application.Json)
                                setBody("""{"urls":["cmd-$index"]}""")
                            }
                            val body = response.bodyAsText()
                            assertEquals(body, HttpStatusCode.OK, response.status)
                            assertEquals(listOf("cmd-$index"), urlsOf(body))
                            assertTrue("busy" !in body)
                        }
                    }
                    assertTrue(
                        "HTTP reads waited or were rejected before the release",
                        awaitLatch(readEntered),
                    )
                    assertTrue(
                        "HTTP commands waited or were rejected before the release",
                        awaitLatch(commandEntered),
                    )
                    release.countDown()
                    withTimeout(5_000) { jobs.forEach { it.join() } }
                }
            }
        } finally {
            release.countDown()
        }
        assertEquals(5, readRuns.get())
        assertEquals((0..2).map { "cmd-$it" }.toSet(), commandCalls.toSet())
    }

    @Test(timeout = 15_000)
    fun readResultFailure_isRecordedWithoutChangingResponse() {
        val diagnostics = ProviderDiagnostics()
        val execution = ProviderExecution(diagnostics = diagnostics)
        testApplication {
            application {
                install(ContentNegotiation) { json(CloudJson) }
                routing {
                    get("/info") {
                        call.respondProviderResult(
                            "GET /info",
                            block = { Result.failure<String>(ContentProviderException("private provider failure")) },
                            execution = execution,
                        ) { value -> call.respondResource(value.wrapAsResponse()) }
                    }
                }
            }
            val response = client.get("/info")
            val body = CloudJson.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(10002, body.getValue("code").jsonPrimitive.content.toInt())
            val snapshot = diagnostics.snapshot(1, "1.0.0", 34)
            assertEquals(0, snapshot.activeCount)
            val record = snapshot.recent.single()
            assertEquals("failed", record.phase)
            assertEquals(ContentProviderException::class.java.name, record.errorType)
            assertTrue(!snapshot.toString().contains("private provider failure"))
        }
    }

    @Test(timeout = 15_000)
    fun contentProviderRoutes_keepBatchOrderAndPartialFailureShape() {
        val diagnostics = ProviderDiagnostics()
        val execution = ProviderExecution(diagnostics = diagnostics)
        val logs = Collections.synchronizedList(mutableListOf<String>())
        val loggingExecution = ProviderExecution(log = { logs.add(it) })
        val calls = Collections.synchronizedList(mutableListOf<String>())
        val behavior = AtomicReference<(String) -> JsonObject?> { url ->
            calls.add(url)
            JsonObject(mapOf("status" to JsonPrimitive(if (url.endsWith("bad")) "error" else "success")))
        }
        testApplication {
            installProbes(loggingExecution, AtomicInteger(), calls, behavior)
            val posted = client.post("/api/contentprovider") {
                contentType(ContentType.Application.Json)
                setBody("""{"urls":["b","bad"],"url":"lifeup://api/tail?token=super-secret"}""")
            }
            assertEquals(HttpStatusCode.OK, posted.status)
            assertEquals(
                listOf("b", "bad", "lifeup://api/tail?token=super-secret"),
                urlsOf(posted.bodyAsText()),
            )
            val rows = CloudJson.parseToJsonElement(posted.bodyAsText()).jsonObject.getValue("data").jsonArray
            assertEquals(
                "error",
                rows[1].jsonObject.getValue("result").jsonObject.getValue("status").jsonPrimitive.content,
            )
            assertEquals(
                listOf("b", "bad", "lifeup://api/tail?token=super-secret"),
                calls.toList(),
            )

            calls.clear()
            val got = client.get("/api/contentprovider") {
                url {
                    parameters.append("url", "z")
                    parameters.append("url", "a")
                }
            }
            assertEquals(HttpStatusCode.OK, got.status)
            assertEquals(listOf("z", "a"), urlsOf(got.bodyAsText()))
            assertEquals(listOf("z", "a"), calls.toList())
        }
        assertTrue(logs.any {
            it.contains("operation=command") &&
                it.contains("route=\"POST /api/contentprovider\"") &&
                it.contains("batchSize=3")
        })
        assertTrue(logs.any {
            it.contains("operation=command") &&
                it.contains("route=\"GET /api/contentprovider\"") &&
                it.contains("batchSize=2")
        })
        assertTrue(logs.none { "lifeup://" in it || "token=" in it || "super-secret" in it || "lane=" in it })

        calls.clear()
        behavior.set { url ->
            calls.add(url)
            if (url == "b") throw ContentProviderException("provider exploded")
            JsonObject(mapOf("ok" to JsonPrimitive(true)))
        }
        testApplication {
            installProbes(execution, AtomicInteger(), calls, behavior)
            val failed = client.post("/api/contentprovider") {
                contentType(ContentType.Application.Json)
                setBody("""{"urls":["a","b","c"]}""")
            }
            val json = CloudJson.parseToJsonElement(failed.bodyAsText()).jsonObject
            assertEquals(HttpStatusCode.OK, failed.status)
            assertEquals(10002, json.getValue("code").jsonPrimitive.content.toInt())
            assertTrue(json.getValue("message").jsonPrimitive.content.contains("provider exploded"))
            assertTrue(json.getValue("data") is JsonNull)
            assertEquals(listOf("a", "b"), calls.toList())
            val postRecord = diagnostics.snapshot(1, "1.0.0", 34).recent.single()
            assertEquals("failed", postRecord.phase)
            assertEquals("POST /api/contentprovider", postRecord.route)
            assertEquals(ContentProviderException::class.java.name, postRecord.errorType)

            calls.clear()
            val failedGet = client.get("/api/contentprovider") {
                url { listOf("a", "b", "c").forEach { parameters.append("url", it) } }
            }
            val getBody = CloudJson.parseToJsonElement(failedGet.bodyAsText()).jsonObject
            assertEquals(HttpStatusCode.OK, failedGet.status)
            assertEquals(10002, getBody.getValue("code").jsonPrimitive.content.toInt())
            assertTrue(getBody.getValue("data") is JsonNull)
            assertEquals(listOf("a", "b"), calls.toList())
            val getRecord = diagnostics.snapshot(1, "1.0.0", 34).recent.last()
            assertEquals("failed", getRecord.phase)
            assertEquals("GET /api/contentprovider", getRecord.route)
            assertEquals(ContentProviderException::class.java.name, getRecord.errorType)

            calls.clear()
            val again = client.post("/api/contentprovider") {
                contentType(ContentType.Application.Json)
                setBody("""{"url":"only"}""")
            }
            assertEquals(HttpStatusCode.OK, again.status)
            assertEquals(listOf("only"), urlsOf(again.bodyAsText()))
        }
    }

    @Test(timeout = 15_000)
    fun emptyCommandRequests_doNotInvokeProvider() {
        val logs = Collections.synchronizedList(mutableListOf<String>())
        val execution = ProviderExecution(log = { logs.add(it) })
        val calls = Collections.synchronizedList(mutableListOf<String>())
        testApplication {
            installProbes(execution, AtomicInteger(), calls)
            val posted = client.post("/api/contentprovider") {
                contentType(ContentType.Application.Json)
                setBody("{}")
            }
            val missing = client.get("/api/contentprovider")
            assertEquals(HttpStatusCode.OK, posted.status)
            assertEquals("[]", dataOf(posted.bodyAsText()))
            assertEquals(HttpStatusCode.OK, missing.status)
            val missingJson = CloudJson.parseToJsonElement(missing.bodyAsText()).jsonObject
            assertEquals(HttpResponse.ERROR, missingJson.getValue("code").jsonPrimitive.content.toInt())
            assertEquals("No url found", missingJson.getValue("message").jsonPrimitive.content)
            assertTrue(missingJson.getValue("data") is JsonNull)
            assertEquals(emptyList<String>(), calls.toList())
        }
        assertTrue(logs.none { it.startsWith("$PROVIDER_LOG_BEGIN ") || it.startsWith("$PROVIDER_LOG_END ") })
    }

    private fun ApplicationTestBuilder.installProbes(
        execution: ProviderExecution,
        readRuns: AtomicInteger,
        commandCalls: MutableList<String> = mutableListOf(),
        behavior: AtomicReference<(String) -> JsonObject?> = AtomicReference { url ->
            commandCalls.add(url)
            JsonObject(mapOf("url" to JsonPrimitive(url)))
        },
    ) {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            routing {
                get("/info") {
                    call.respondProviderResult(
                        "GET /info",
                        execution = execution,
                        block = { Result.success(readRuns.incrementAndGet()) },
                    ) { value ->
                        call.respondResource(value.wrapAsResponse())
                    }
                }
                get("/api/contentprovider") {
                    call.respondContentProviderGet(
                        call.request.queryParameters.getAll("url"),
                        execution,
                    ) { url ->
                        behavior.get().invoke(url)
                    }
                }
                post<RawQueryVo>("/api/contentprovider") { body ->
                    call.respondContentProviderPost(body, execution) { url ->
                        behavior.get().invoke(url)
                    }
                }
            }
        }
    }

    private fun dataOf(body: String): String {
        return CloudJson.parseToJsonElement(body).jsonObject.getValue("data").toString()
    }

    private fun urlsOf(body: String): List<String> {
        return CloudJson.parseToJsonElement(body).jsonObject.getValue("data").jsonArray.map { row ->
            row.jsonObject.getValue("url").jsonPrimitive.content
        }
    }

    private suspend fun awaitLatch(latch: CountDownLatch): Boolean {
        return withContext(Dispatchers.IO) { latch.await(5, TimeUnit.SECONDS) }
    }
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
