package net.lifeupapp.lifeup.api.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 安全地解析 JSON 字符串为指定类型,解析失败时返回 null
 */
inline fun <reified T> String.decodeFromStringOrNull(): T? {
    return runCatching {
        json.decodeFromString<T>(this)
    }.getOrNull()
}

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
