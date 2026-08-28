package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import kotlinx.serialization.decodeFromString
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull
import net.lifeupapp.lifeup.api.utils.json

class LevelDefinesApi(private val context: Context) : ContentProviderApi {

    fun getDefines(): Result<LevelDefines> {
        return try {
            var found: LevelDefines? = null
            context.forEachContent(ContentProviderUrl.LEVEL_DEFINES) {
                found = LevelDefines(
                    custom = it.getIntOrNull("custom") == 1,
                    levels = runCatching {
                        json.decodeFromString<List<LevelDefine>>(it.getStringOrNull("levels") ?: "[]")
                    }.getOrDefault(emptyList()),
                )
            }
            found?.let { Result.success(it) } ?: Result.failure(IllegalAccessException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
