package net.lifeupapp.lifeup.api.utils

import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.api.BuildConfig

/**
 * Safely decode a JSON string into the requested type and return null on failure.
 */
inline fun <reified T> String.decodeFromStringOrNull(): T? {
    val result = runCatching {
        json.decodeFromString<T>(this)
    }
    if (BuildConfig.DEBUG) {
        return result.getOrThrow()
    }
    return result.getOrNull()
}

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
