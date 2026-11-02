package net.lifeupapp.lifeup.http.service

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.lifeupapp.lifeup.api.content.tasks.SmartListType
import net.lifeupapp.lifeup.api.content.tasks.Task
import net.lifeupapp.lifeup.api.exception.ContentProviderException
import net.lifeupapp.lifeup.api.exception.NullCursorException
import net.lifeupapp.lifeup.http.vo.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartListRouteTest {

    @Test
    fun validType_callsSdkAndReturnsResourceEnvelope() = testApplication {
        val seen = mutableListOf<SmartListType>()
        installSmartLists { type ->
            seen += type
            Result.success(listOf(task(type.pathSegment)))
        }

        SmartListType.entries.forEach { type ->
            val response = client.get("/smart_lists/${type.pathSegment}/tasks")
            val body = response.bodyAsText()
            val json = CloudJson.parseToJsonElement(body).jsonObject
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(HttpResponse.SUCCESS, json.getValue("code").jsonPrimitive.content.toInt())
            assertTrue(body.contains("\"name\":\"${type.pathSegment}\""))
            assertFalse(response.headers[HttpHeaders.ETag].isNullOrBlank())
            assertEquals(RESOURCE_CACHE_CONTROL, response.headers[HttpHeaders.CacheControl])
        }

        assertEquals(SmartListType.entries.toList(), seen)
    }

    @Test
    fun invalidType_usesErrorEnvelopeAndDoesNotQuery() = testApplication {
        var calls = 0
        installSmartLists {
            calls += 1
            Result.success(listOf(task("should-not-run")))
        }

        listOf("Daily", "DAILY", "yearly", "all", "tasks").forEach { raw ->
            val response = client.get("/smart_lists/$raw/tasks")
            val json = CloudJson.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(HttpResponse.ERROR, json.getValue("code").jsonPrimitive.content.toInt())
            assertTrue(json.getValue("message").jsonPrimitive.content.contains("invalid parameter"))
            assertTrue(json.getValue("data") is JsonNull)
            assertNull(response.headers[HttpHeaders.ETag])
            assertNull(response.headers[HttpHeaders.CacheControl])
        }

        assertEquals(0, calls)
    }

    @Test
    fun sdkFailure_preservesProviderErrorWithoutFallback() = testApplication {
        val seen = mutableListOf<SmartListType>()
        installSmartLists { type ->
            seen += type
            when (type) {
                SmartListType.WEEKLY -> Result.failure(NullCursorException())
                SmartListType.MONTHLY -> Result.failure(ContentProviderException("unknown smart list uri"))
                SmartListType.DAILY -> Result.success(emptyList())
            }
        }

        val missing = CloudJson.parseToJsonElement(
            client.get("/smart_lists/weekly/tasks").bodyAsText(),
        ).jsonObject
        assertEquals(10001, missing.getValue("code").jsonPrimitive.content.toInt())
        assertTrue(missing.getValue("data") is JsonNull)
        assertTrue(missing.getValue("message").jsonPrimitive.content.contains("Cursor is null"))

        val rejected = CloudJson.parseToJsonElement(
            client.get("/smart_lists/monthly/tasks").bodyAsText(),
        ).jsonObject
        assertEquals(10002, rejected.getValue("code").jsonPrimitive.content.toInt())
        assertTrue(rejected.getValue("data") is JsonNull)
        assertTrue(rejected.getValue("message").jsonPrimitive.content.contains("unknown smart list uri"))

        val empty = CloudJson.parseToJsonElement(
            client.get("/smart_lists/daily/tasks").bodyAsText(),
        ).jsonObject
        assertEquals(HttpResponse.SUCCESS, empty.getValue("code").jsonPrimitive.content.toInt())
        assertEquals("[]", empty.getValue("data").toString())
        assertEquals(
            listOf(SmartListType.WEEKLY, SmartListType.MONTHLY, SmartListType.DAILY),
            seen,
        )
    }

    @Test
    fun authGate_matchesOtherResources() = testApplication {
        var calls = 0
        installSmartLists(token = "secret") {
            calls += 1
            Result.success(listOf(task("daily")))
        }

        val missing = client.get("/smart_lists/daily/tasks")
        assertEquals(HttpStatusCode.Unauthorized, missing.status)
        assertTrue(missing.bodyAsText().contains("Invalid API token"))
        assertEquals(0, calls)

        val allowed = client.get("/smart_lists/daily/tasks") {
            header(HttpHeaders.Authorization, "secret")
        }
        assertEquals(HttpStatusCode.OK, allowed.status)
        assertTrue(allowed.bodyAsText().contains("\"name\":\"daily\""))
        assertEquals(1, calls)
    }

    private fun ApplicationTestBuilder.installSmartLists(
        token: String? = null,
        queryTasks: (SmartListType) -> Result<List<Task>>,
    ) {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            if (token != null) {
                installApiTokenValidation(token)
            }
            routing {
                registerSmartListTasksRoute(queryTasks)
            }
        }
    }

    private fun task(name: String): Task = Task.builder { setName(name) }
}
