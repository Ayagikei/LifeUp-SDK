package net.lifeupapp.lifeup.http.service

import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import net.lifeupapp.lifeup.http.vo.HttpResponse

private const val DIAGNOSTICS_CACHE_CONTROL = "no-store"

/**
 * Read-only `GET /diagnostics`. Auth is the process-wide
 * [installApiTokenValidation] plugin; this route does not add a credential.
 * The handler reads [diagnostics] and returns. It does not call a
 * ContentProvider, `/info`, or a database, and it does not attach an ETag.
 */
internal fun Route.registerDiagnosticsRoute(
    diagnostics: ProviderDiagnostics = sharedProviderDiagnostics,
    cloudVersion: Int,
    cloudVersionName: String,
    androidApi: Int,
) {
    get("/diagnostics") {
        val snapshot = diagnostics.snapshot(
            cloudVersion = cloudVersion,
            cloudVersionName = cloudVersionName,
            androidApi = androidApi,
        )
        call.response.header(HttpHeaders.CacheControl, DIAGNOSTICS_CACHE_CONTROL)
        call.respond(HttpResponse.success(snapshot))
    }
}
