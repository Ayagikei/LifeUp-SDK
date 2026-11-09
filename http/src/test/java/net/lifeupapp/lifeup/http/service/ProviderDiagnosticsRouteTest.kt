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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.lifeupapp.lifeup.http.vo.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderDiagnosticsRouteTest {

    @Test
    fun unauthorized_isRejectedBeforeSnapshot() = testApplication {
        val diagnostics = ProviderDiagnostics {
            throw AssertionError("diagnostics must not run when the token is rejected")
        }
        installRoute(diagnostics, token = "gate-token")

        val missing = client.get("/diagnostics")
        val missingBody = missing.bodyAsText()
        assertEquals(HttpStatusCode.Unauthorized, missing.status)
        assertTrue(missingBody.contains("Invalid API token"))
        assertFalse(missingBody.contains("gate-token"))

        val wrongHeader = client.get("/diagnostics") {
            header(HttpHeaders.Authorization, "nope")
        }
        val wrongHeaderBody = wrongHeader.bodyAsText()
        assertEquals(HttpStatusCode.Unauthorized, wrongHeader.status)
        assertFalse(wrongHeaderBody.contains("nope"))

        val wrongQuery = client.get("/diagnostics?token=nope")
        val wrongQueryBody = wrongQuery.bodyAsText()
        assertEquals(HttpStatusCode.Unauthorized, wrongQuery.status)
        assertFalse(wrongQueryBody.contains("nope"))
    }

    @Test
    fun authorized_returnsNoStoreSchemaWithoutEtagOrProvider() = testApplication {
        var nanos = 0L
        val diagnostics = ProviderDiagnostics { nanos }
        val secret = "raw-token-value"
        nanos = 0L
        diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /tasks")
        nanos = millis(1_000)
        val command = diagnostics.dispatch(
            PROVIDER_OPERATION_COMMAND,
            "lifeup://api?token=$secret",
            batchSize = 3,
        )
        diagnostics.begin(command)
        nanos = millis(2_500)
        val failed = diagnostics.dispatch(PROVIDER_OPERATION_READ, "GET /coin")
        diagnostics.begin(failed)
        diagnostics.complete(failed, IllegalStateException("account=$secret task=买菜"))
        val expected = diagnostics.snapshot(12, "12.0.4", 34)
        installRoute(
            diagnostics,
            token = "gate-token",
            cloudVersion = 12,
            cloudVersionName = "12.0.4",
            androidApi = 34,
        )

        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/tasks") { header(HttpHeaders.Authorization, "gate-token") }.status,
        )
        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/info") { header(HttpHeaders.Authorization, "gate-token") }.status,
        )
        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/api/contentprovider") {
                header(HttpHeaders.Authorization, "gate-token")
            }.status,
        )

        val response = client.get("/diagnostics") {
            header(HttpHeaders.Authorization, "gate-token")
        }
        val body = response.bodyAsText()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
        assertNull(response.headers[HttpHeaders.ETag])
        assertNull(response.headers[HttpHeaders.LastModified])
        assertFalse(body.contains(secret))
        assertFalse(body.contains("买菜"))
        assertFalse(body.contains("account="))
        assertFalse(body.contains("lifeup://"))
        assertFalse(body.contains("gate-token"))
        assertEquals(CloudJson.encodeToString(HttpResponse.success(expected)), body)

        val parsed = CloudJson.parseToJsonElement(body).jsonObject
        assertEquals(HttpResponse.SUCCESS, parsed.getValue("code").jsonPrimitive.content.toInt())
        assertEquals("success", parsed.getValue("message").jsonPrimitive.content)
        val data = parsed.getValue("data").jsonObject
        assertEquals("1", data.getValue("schemaVersion").jsonPrimitive.content)
        assertEquals("dispatchers_io", data.getValue("executionMode").jsonPrimitive.content)
        assertEquals("10000", data.getValue("slowThresholdMs").jsonPrimitive.content)
        assertEquals("12", data.getValue("cloudVersion").jsonPrimitive.content)
        assertEquals("12.0.4", data.getValue("cloudVersionName").jsonPrimitive.content)
        assertEquals("34", data.getValue("androidApi").jsonPrimitive.content)
        assertEquals("2", data.getValue("activeCount").jsonPrimitive.content)
        assertEquals("0", data.getValue("activeTruncatedCount").jsonPrimitive.content)
        assertEquals(2, data.getValue("active").jsonArray.size)
        assertEquals(1, data.getValue("recent").jsonArray.size)
        assertTrue(body.contains("\"dispatchMs\":null"))
        assertTrue(body.contains("\"dispatchMs\":0"))
        assertTrue(body.contains("\"phase\":\"dispatching\""))
        assertTrue(body.contains("\"phase\":\"executing\""))
        assertTrue(body.contains("\"phase\":\"failed\""))
        assertTrue(body.contains("\"errorType\":\"java.lang.IllegalStateException\""))
        assertTrue(body.contains("\"batchSize\":3"))
        assertTrue(body.contains("\"route\":\"${sanitizeProviderRoute("token")}\""))

        val decoded = CloudJson.decodeFromString<HttpResponse<ProviderDiagnosticsSnapshot>>(body)
        val strict = Json { encodeDefaults = false }
        val reencoded = strict.encodeToString(requireNotNull(decoded.data))
        assertTrue(reencoded.contains("\"schemaVersion\":1"))
        assertTrue(reencoded.contains("\"executionMode\":\"dispatchers_io\""))
        assertTrue(reencoded.contains("\"slowThresholdMs\":10000"))

        val starred = client.get("/diagnostics") {
            header(HttpHeaders.Authorization, "gate-token")
            header(HttpHeaders.IfNoneMatch, "*")
        }
        assertEquals(HttpStatusCode.OK, starred.status)
        assertEquals(body, starred.bodyAsText())
        assertNull(starred.headers[HttpHeaders.ETag])
        assertEquals("no-store", starred.headers[HttpHeaders.CacheControl])

        val query = client.get("/diagnostics?token=gate-token")
        assertEquals(HttpStatusCode.OK, query.status)
        assertEquals(body, query.bodyAsText())
    }

    @Test
    fun defaultRecorder_readsSharedMetadataWithoutItsOwnCredential() = testApplication {
        val before = sharedProviderDiagnostics.snapshot(1, "n", 1)
        installRoute(
            useShared = true,
            cloudVersion = 3,
            cloudVersionName = "3.0.1",
            androidApi = 30,
        )

        val response = client.get("/diagnostics")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
        assertNull(response.headers[HttpHeaders.ETag])
        val after = sharedProviderDiagnostics.snapshot(1, "n", 1)
        assertEquals(before.activeCount, after.activeCount)
        assertEquals(before.activeTruncatedCount, after.activeTruncatedCount)
        assertEquals(before.recent.map { it.id }, after.recent.map { it.id })
        assertEquals(before.active.map { it.id }, after.active.map { it.id })

        val data = CloudJson.parseToJsonElement(response.bodyAsText())
            .jsonObject
            .getValue("data")
            .jsonObject
        assertEquals("1", data.getValue("schemaVersion").jsonPrimitive.content)
        assertEquals("dispatchers_io", data.getValue("executionMode").jsonPrimitive.content)
        assertEquals("10000", data.getValue("slowThresholdMs").jsonPrimitive.content)
        assertEquals("3", data.getValue("cloudVersion").jsonPrimitive.content)
        assertEquals("3.0.1", data.getValue("cloudVersionName").jsonPrimitive.content)
        assertEquals("30", data.getValue("androidApi").jsonPrimitive.content)
    }

    private fun ApplicationTestBuilder.installRoute(
        diagnostics: ProviderDiagnostics = ProviderDiagnostics(),
        token: String? = null,
        cloudVersion: Int = 1,
        cloudVersionName: String = "1",
        androidApi: Int = 1,
        useShared: Boolean = false,
    ) {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            if (token != null) {
                installApiTokenValidation(token)
            }
            routing {
                if (useShared) {
                    registerDiagnosticsRoute(
                        cloudVersion = cloudVersion,
                        cloudVersionName = cloudVersionName,
                        androidApi = androidApi,
                    )
                } else {
                    registerDiagnosticsRoute(
                        diagnostics = diagnostics,
                        cloudVersion = cloudVersion,
                        cloudVersionName = cloudVersionName,
                        androidApi = androidApi,
                    )
                }
            }
        }
    }

    private fun millis(value: Long): Long = value * 1_000_000L
}
