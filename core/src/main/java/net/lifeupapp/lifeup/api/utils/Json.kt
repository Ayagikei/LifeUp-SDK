package net.lifeupapp.lifeup.api.utils

import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.api.BuildConfig

/**
 * 安全地解析 JSON 字符串为指定类型,解析失败时返回 null
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
