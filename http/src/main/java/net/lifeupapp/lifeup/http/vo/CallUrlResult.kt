package net.lifeupapp.lifeup.http.vo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * CallUrlResult
 */
@Serializable
data class CallUrlResult(
    val url: String,
    val result: JsonObject?
)