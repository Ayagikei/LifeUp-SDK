package net.lifeupapp.lifeup.http.service

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.lifeupapp.lifeup.http.vo.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class ResourceConditionalGetTest {

    @Test
    fun plainGet_returnsEnvelopeQuotedEtagAndPrivateNoCache() = testApplication {
        installNotes()
        val response = client.get("/notes")
        val bytes = response.readBytes()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expected("任务"), bytes.toString(Charsets.UTF_8))
        assertTrue(bytes.toString(Charsets.UTF_8).startsWith("{"))
        assertEquals(RESOURCE_CACHE_CONTROL, response.headers[HttpHeaders.CacheControl])
        assertNull(response.headers[HttpHeaders.LastModified])
        assertEquals(sha256(bytes), opaque(response.headers[HttpHeaders.ETag]))
    }

    @Test
    fun matchingTag_returns304WithEmptyBodyAndValidators() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = first.headers[HttpHeaders.ETag]
        val cached = sha256(first.readBytes())
        val again = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.NotModified, again.status)
        assertEquals(0, again.readBytes().size)
        assertEquals(etag, again.headers[HttpHeaders.ETag])
        assertEquals("\"$cached\"", again.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, again.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun changedPayloadAndNonmatchingTags_return200() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = first.headers[HttpHeaders.ETag]
        first.readBytes()
        val other = client.get("/notes?title=其他") {
            header(HttpHeaders.IfNoneMatch, etag)
        }
        val otherBytes = other.readBytes()
        assertEquals(HttpStatusCode.OK, other.status)
        assertEquals(expected("其他"), otherBytes.toString(Charsets.UTF_8))
        assertFalse(etag == other.headers[HttpHeaders.ETag])

        val miss = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "\"0000000000000000000000000000000000000000000000000000000000000000\"")
        }
        assertEquals(HttpStatusCode.OK, miss.status)
        assertEquals(expected("任务"), miss.bodyAsText())
    }

    @Test
    fun weakListAndQuotedIfNoneMatch_followKtor() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        first.readBytes()

        val quoted = client.get("/notes") { header(HttpHeaders.IfNoneMatch, etag) }
        assertEquals(HttpStatusCode.NotModified, quoted.status)
        assertEquals(0, quoted.readBytes().size)

        val weak = client.get("/notes") { header(HttpHeaders.IfNoneMatch, "W/$etag") }
        assertEquals(HttpStatusCode.NotModified, weak.status)
        assertEquals(0, weak.readBytes().size)

        val list = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "\"nope\", $etag")
        }
        assertEquals(HttpStatusCode.NotModified, list.status)
        assertEquals(0, list.readBytes().size)

        val weakList = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "\"nope\", W/$etag")
        }
        assertEquals(HttpStatusCode.NotModified, weakList.status)
        assertEquals(etag, weakList.headers[HttpHeaders.ETag])
    }

    @Test
    fun wildcardIfNoneMatch_returns304WithEmptyBodyAndValidators() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        val cached = sha256(first.readBytes())

        val star = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "*")
        }
        assertEquals(HttpStatusCode.NotModified, star.status)
        assertEquals(0, star.readBytes().size)
        assertEquals(etag, star.headers[HttpHeaders.ETag])
        assertEquals("\"$cached\"", star.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, star.headers[HttpHeaders.CacheControl])
        assertNull(star.headers[HttpHeaders.LastModified])

        val list = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "*, \"nope\"")
        }
        assertEquals(HttpStatusCode.NotModified, list.status)
        assertEquals(0, list.readBytes().size)
        assertEquals(etag, list.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, list.headers[HttpHeaders.CacheControl])

        // EntityTagVersion.parseSingle does not treat W/* as STAR.
        val weakStar = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "W/*")
        }
        assertEquals(HttpStatusCode.OK, weakStar.status)
        assertEquals(expected("任务"), weakStar.readBytes().toString(Charsets.UTF_8))
    }

    @Test
    fun ifMatch_isEvaluatedWhenWildcardNoneMatchIsOtherwiseOk() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        first.readBytes()
        val wrong = "\"0000000000000000000000000000000000000000000000000000000000000000\""

        val mismatch = client.get("/notes") {
            header(HttpHeaders.IfMatch, wrong)
        }
        assertEquals(HttpStatusCode.PreconditionFailed, mismatch.status)
        assertEquals(0, mismatch.readBytes().size)

        val starThenMismatch = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "*")
            header(HttpHeaders.IfMatch, wrong)
        }
        assertEquals(HttpStatusCode.PreconditionFailed, starThenMismatch.status)
        assertEquals(0, starThenMismatch.readBytes().size)

        val starThenMatch = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "*")
            header(HttpHeaders.IfMatch, etag)
        }
        assertEquals(HttpStatusCode.NotModified, starThenMatch.status)
        assertEquals(0, starThenMatch.readBytes().size)
        assertEquals(etag, starThenMatch.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, starThenMatch.headers[HttpHeaders.CacheControl])

        val starThenAny = client.get("/notes") {
            header(HttpHeaders.IfNoneMatch, "*")
            header(HttpHeaders.IfMatch, "*")
        }
        assertEquals(HttpStatusCode.NotModified, starThenAny.status)
        assertEquals(0, starThenAny.readBytes().size)
        assertEquals(etag, starThenAny.headers[HttpHeaders.ETag])
    }

    @Test
    fun failedIfMatch_precedesMatchingIfNoneMatch() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        first.readBytes()
        val wrong = "\"0000000000000000000000000000000000000000000000000000000000000000\""

        val failedMatch = client.get("/notes") {
            header(HttpHeaders.IfMatch, wrong)
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.PreconditionFailed, failedMatch.status)
        assertEquals(0, failedMatch.readBytes().size)

        val bothMatch = client.get("/notes") {
            header(HttpHeaders.IfMatch, etag)
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.NotModified, bothMatch.status)
        assertEquals(0, bothMatch.readBytes().size)
        assertEquals(etag, bothMatch.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, bothMatch.headers[HttpHeaders.CacheControl])

        val matchThenMiss = client.get("/notes") {
            header(HttpHeaders.IfMatch, etag)
            header(HttpHeaders.IfNoneMatch, wrong)
        }
        val body = matchThenMiss.readBytes()
        assertEquals(HttpStatusCode.OK, matchThenMiss.status)
        assertEquals(expected("任务"), body.toString(Charsets.UTF_8))
        assertEquals(etag, matchThenMiss.headers[HttpHeaders.ETag])
    }

    @Test
    fun weakIfMatch_sameOpaqueValue_isPreconditionFailed() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        first.readBytes()

        val weakOnly = client.get("/notes") {
            header(HttpHeaders.IfMatch, "W/$etag")
        }
        assertEquals(HttpStatusCode.PreconditionFailed, weakOnly.status)
        assertEquals(0, weakOnly.readBytes().size)

        val weakThenNone = client.get("/notes") {
            header(HttpHeaders.IfMatch, "W/$etag")
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.PreconditionFailed, weakThenNone.status)
        assertEquals(0, weakThenNone.readBytes().size)

        val weakList = client.get("/notes") {
            header(HttpHeaders.IfMatch, "\"nope\", W/$etag")
        }
        assertEquals(HttpStatusCode.PreconditionFailed, weakList.status)
        assertEquals(0, weakList.readBytes().size)
    }

    @Test
    fun strongListAndWildcardIfMatch_allowRepresentation() = testApplication {
        installNotes()
        val first = client.get("/notes")
        val etag = requireNotNull(first.headers[HttpHeaders.ETag])
        val bytes = first.readBytes()
        val text = bytes.toString(Charsets.UTF_8)

        val strong = client.get("/notes") { header(HttpHeaders.IfMatch, etag) }
        assertEquals(HttpStatusCode.OK, strong.status)
        assertEquals(text, strong.readBytes().toString(Charsets.UTF_8))
        assertEquals(etag, strong.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, strong.headers[HttpHeaders.CacheControl])

        val list = client.get("/notes") {
            header(HttpHeaders.IfMatch, "\"nope\", $etag")
        }
        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(text, list.readBytes().toString(Charsets.UTF_8))
        assertEquals(etag, list.headers[HttpHeaders.ETag])

        val wildcard = client.get("/notes") { header(HttpHeaders.IfMatch, "*") }
        assertEquals(HttpStatusCode.OK, wildcard.status)
        assertEquals(text, wildcard.readBytes().toString(Charsets.UTF_8))
        assertEquals(etag, wildcard.headers[HttpHeaders.ETag])
        assertEquals(RESOURCE_CACHE_CONTROL, wildcard.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun utf8HashMatchesBytes_andJsonDefaultsMatchNegotiation() = testApplication {
        installNotes()
        val negotiated = client.get("/plain")
        val negotiatedBytes = negotiated.readBytes()
        val resource = client.get("/notes")
        val resourceBytes = resource.readBytes()
        assertEquals(negotiatedBytes.toList(), resourceBytes.toList())
        assertEquals(sha256(resourceBytes), opaque(resource.headers[HttpHeaders.ETag]))
        assertTrue(resourceBytes.toString(Charsets.UTF_8).contains("\"pinned\":false"))
        assertTrue(resourceBytes.toString(Charsets.UTF_8).contains("任务"))
        assertNull(negotiated.headers[HttpHeaders.ETag])
        assertNull(negotiated.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun businessErrorAndAuthFailure_never304() = testApplication {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            installApiTokenValidation("secret")
            routing {
                get("/notes") { call.respondResource(HttpResponse.success(Note("任务"))) }
                get("/error") { call.respond(HttpResponse.error<String>("provider down", 10002)) }
                get("/guard") { call.respondResource(HttpResponse.error<String>("provider down", 10002)) }
            }
        }
        val ok = client.get("/notes") { header(HttpHeaders.Authorization, "secret") }
        val etag = ok.headers[HttpHeaders.ETag]
        ok.readBytes()

        val starredError = client.get("/error") {
            header(HttpHeaders.Authorization, "secret")
            header(HttpHeaders.IfNoneMatch, "*")
        }
        assertEquals(HttpStatusCode.OK, starredError.status)
        assertTrue(starredError.bodyAsText().contains("\"code\":10002"))
        assertNull(starredError.headers[HttpHeaders.ETag])

        val taggedError = client.get("/error") {
            header(HttpHeaders.Authorization, "secret")
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.OK, taggedError.status)
        assertTrue(taggedError.bodyAsText().contains("provider down"))
        assertNull(taggedError.headers[HttpHeaders.ETag])

        val guarded = client.get("/guard") {
            header(HttpHeaders.Authorization, "secret")
            header(HttpHeaders.IfNoneMatch, "*")
        }
        assertEquals(HttpStatusCode.OK, guarded.status)
        assertTrue(guarded.bodyAsText().contains("\"code\":10002"))
        assertNull(guarded.headers[HttpHeaders.ETag])
        assertNull(guarded.headers[HttpHeaders.CacheControl])

        val noToken = client.get("/notes") { header(HttpHeaders.IfNoneMatch, "*") }
        assertEquals(HttpStatusCode.Unauthorized, noToken.status)
        assertTrue(noToken.bodyAsText().contains("Invalid API token"))

        val badToken = client.get("/notes") {
            header(HttpHeaders.Authorization, "nope")
            header(HttpHeaders.IfNoneMatch, etag)
        }
        assertEquals(HttpStatusCode.Unauthorized, badToken.status)
        assertTrue(badToken.bodyAsText().contains("Invalid API token"))

        val queryToken = client.get("/notes?token=secret")
        assertEquals(HttpStatusCode.OK, queryToken.status)
        assertEquals(etag, queryToken.headers[HttpHeaders.ETag])
    }

    @Test
    fun excludedRoutes_doNotAcquireConditionalBehavior() = testApplication {
        installNotes()
        for (path in listOf("/api", "/events")) {
            val first = client.get(path)
            val bytes = first.readBytes()
            assertEquals(HttpStatusCode.OK, first.status)
            assertNull(first.headers[HttpHeaders.ETag])
            assertNull(first.headers[HttpHeaders.CacheControl])
            val again = client.get(path) {
                header(HttpHeaders.IfNoneMatch, "\"${sha256(bytes)}\"")
            }
            assertEquals(HttpStatusCode.OK, again.status)
            assertEquals(bytes.toList(), again.readBytes().toList())
            val star = client.get(path) { header(HttpHeaders.IfNoneMatch, "*") }
            assertEquals(HttpStatusCode.OK, star.status)
            assertEquals(bytes.toList(), star.readBytes().toList())
            assertNull(star.headers[HttpHeaders.ETag])
            assertNull(star.headers[HttpHeaders.CacheControl])
        }
    }

    @Test
    fun cors_allowsIfNoneMatchAndExposesEtagWhenEnabled() = testApplication {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            install(CORS) { configureCloudCors() }
            routing {
                get("/notes") { call.respondResource(HttpResponse.success(Note("任务"))) }
            }
        }
        val preflight = client.options("/notes") {
            header(HttpHeaders.Origin, "http://example.com")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Get.value)
            header(HttpHeaders.AccessControlRequestHeaders, HttpHeaders.IfNoneMatch)
        }
        val allow = preflight.headers[HttpHeaders.AccessControlAllowHeaders].orEmpty()
        assertTrue(allow.contains("If-None-Match", ignoreCase = true))
        assertTrue(preflight.headers[HttpHeaders.AccessControlAllowOrigin] == "*" ||
            preflight.headers[HttpHeaders.AccessControlAllowOrigin] == "http://example.com")

        val ok = client.get("/notes") {
            header(HttpHeaders.Origin, "http://example.com")
        }
        val expose = ok.headers[HttpHeaders.AccessControlExposeHeaders].orEmpty()
        assertTrue(expose.contains("ETag", ignoreCase = true))
        assertFalse(ok.headers[HttpHeaders.ETag].isNullOrBlank())
    }

    private fun ApplicationTestBuilder.installNotes() {
        application {
            install(ContentNegotiation) { json(CloudJson) }
            installResourceConditionalGet()
            routing {
                get("/notes") {
                    val title = call.request.queryParameters["title"] ?: "任务"
                    call.respondResource(HttpResponse.success(Note(title)))
                }
                get("/plain") { call.respond(HttpResponse.success(Note("任务"))) }
                get("/api") { call.respond(HttpResponse.success("success")) }
                get("/events") { call.respond(HttpResponse.success("event")) }
            }
        }
    }

    private fun expected(title: String): String =
        CloudJson.encodeToString(HttpResponse.success(Note(title)))

    private fun opaque(header: String?): String {
        val value = requireNotNull(header)
        assertFalse(value.startsWith("W/"))
        assertTrue(value.startsWith("\"") && value.endsWith("\""))
        return value.substring(1, value.length - 1)
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    @Serializable
    private data class Note(val title: String, val pinned: Boolean = false)
}
