package net.lifeupapp.lifeup.http.service

import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.EntityTagVersion
import io.ktor.http.content.TextContent
import io.ktor.http.content.Version
import io.ktor.http.content.VersionCheckResult
import io.ktor.http.content.versions
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.cors.CORSConfig
import io.ktor.server.response.header
import io.ktor.server.response.respond
import kotlinx.serialization.encodeToString
import net.lifeupapp.lifeup.http.vo.HttpResponse
import java.security.MessageDigest

/** Same instance ContentNegotiation uses via [io.ktor.serialization.kotlinx.json.json]. */
internal val CloudJson = DefaultJson

internal const val RESOURCE_CACHE_CONTROL = "private, no-cache"

internal fun Application.installResourceConditionalGet() {
    // Default providers read OutgoingContent.versions. ConditionalHeaders still converts
    // VersionCheckResult into 304/412. ResourceEntityTagVersion only decides the result.
    install(ConditionalHeaders)
}

internal fun CORSConfig.configureCloudCors() {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.AccessControlAllowOrigin)
    allowHeader(HttpHeaders.IfNoneMatch)
    exposeHeader(HttpHeaders.ETag)
    anyHost()
}

internal fun Application.installApiTokenValidation(apiToken: String) {
    if (apiToken.isBlank()) return
    install(createApplicationPlugin("ApiTokenValidation") {
        onCall { call ->
            val authHeader = call.request.headers[HttpHeaders.Authorization]
            val queryToken = call.request.queryParameters["token"]
            if (authHeader != apiToken && queryToken != apiToken) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    HttpResponse.error<String>(
                        "Invalid API token",
                        HttpStatusCode.Unauthorized.value
                    )
                )
            }
        }
    })
}

/**
 * Serializes a successful envelope once with [CloudJson] and attaches a strong
 * entity tag of those UTF-8 bytes. Error envelopes stay on normal [respond]
 * so ConditionalHeaders has no version to turn into 304.
 */
internal suspend inline fun <reified T> ApplicationCall.respondResource(body: HttpResponse<T>) {
    if (body.code != HttpResponse.SUCCESS) {
        respond(body)
        return
    }
    val text = CloudJson.encodeToString(body)
    val content = TextContent(
        text,
        ContentType.Application.Json.withCharset(Charsets.UTF_8),
    )
    content.versions = listOf(
        ResourceEntityTagVersion(EntityTagVersion(sha256Hex(content.bytes()), weak = false)),
    )
    response.header(HttpHeaders.CacheControl, RESOURCE_CACHE_CONTROL)
    respond(content)
}

/**
 * Successful-resource validator. [EntityTagVersion.parse] reads the header and
 * [EntityTagVersion.appendHeadersTo] writes `ETag`. [EntityTagVersion.check] is not used:
 * Ktor 2.3.12 evaluates If-None-Match first, and [EntityTagVersion.match] ignores weakness.
 *
 * If-Match is first. `*` passes because this version is attached only to an existing
 * successful representation. Any other candidate must be a strong opaque-tag match.
 * If-None-Match is next, using weak comparison; `*` is [VersionCheckResult.NOT_MODIFIED]
 * for these GET representations. [ConditionalHeaders] converts the result.
 */
internal class ResourceEntityTagVersion(
    private val delegate: EntityTagVersion,
) : Version {
    override fun check(requestHeaders: Headers): VersionCheckResult {
        val ifMatch = requestHeaders[HttpHeaders.IfMatch]
        if (ifMatch != null && !ifMatchPasses(EntityTagVersion.parse(ifMatch))) {
            return VersionCheckResult.PRECONDITION_FAILED
        }
        val ifNoneMatch = requestHeaders[HttpHeaders.IfNoneMatch]
        if (ifNoneMatch != null && ifNoneMatchHits(EntityTagVersion.parse(ifNoneMatch))) {
            return VersionCheckResult.NOT_MODIFIED
        }
        return VersionCheckResult.OK
    }

    override fun appendHeadersTo(builder: HeadersBuilder) {
        delegate.appendHeadersTo(builder)
    }

    private fun ifMatchPasses(given: List<EntityTagVersion>): Boolean {
        if (given.isEmpty()) return true
        if (EntityTagVersion.STAR in given) return true
        return given.any(::stronglyMatches)
    }

    private fun ifNoneMatchHits(given: List<EntityTagVersion>): Boolean {
        if (EntityTagVersion.STAR in given) return true
        return given.any(::weaklyMatches)
    }

    private fun stronglyMatches(candidate: EntityTagVersion): Boolean {
        if (delegate.weak || candidate.weak || candidate == EntityTagVersion.STAR) return false
        return delegate.match(candidate)
    }

    private fun weaklyMatches(candidate: EntityTagVersion): Boolean {
        if (candidate == EntityTagVersion.STAR) return false
        return delegate.match(candidate)
    }
}

internal fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    val out = CharArray(digest.size * 2)
    var i = 0
    for (b in digest) {
        val v = b.toInt() and 0xff
        out[i++] = HEX[v ushr 4]
        out[i++] = HEX[v and 0x0f]
    }
    return String(out)
}

private val HEX = "0123456789abcdef".toCharArray()
