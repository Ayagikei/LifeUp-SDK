package net.lifeupapp.lifeup.http.vo

import kotlinx.serialization.Serializable

@Serializable
data class RawQueryVo(
    val url: String? = null,
    val urls: List<String>? = null
)
