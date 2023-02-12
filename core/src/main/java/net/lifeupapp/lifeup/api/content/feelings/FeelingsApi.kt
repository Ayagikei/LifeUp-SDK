package net.lifeupapp.lifeup.api.content.feelings

import android.content.Context
import android.net.Uri
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import kotlinx.serialization.decodeFromString
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.json

class FeelingsApi(private val context: Context) : ContentProviderApi {

    fun listFeelings(offset: Int = 0, limit: Int = 100): Result<List<Feelings>> {
        val feelings = mutableListOf<Feelings>()
        try {

            val uri = Uri.parse(ContentProviderUrl.FEELINGS).buildUpon()
                .appendQueryParameter("offset", offset.toString())
                .appendQueryParameter("limit", limit.toString())
                .build()

            context.forEachContent(uri.toString()) {
                val id = it.getLongOrNull(0)
                val content = it.getStringOrNull(1)
                val isFav = it.getIntOrNull(2)
                val title = it.getStringOrNull(3)
                val time = it.getLongOrNull(4)
                val attachments = it.getStringOrNull(5)
                val type = it.getIntOrNull(6)

                feelings.add(
                    Feelings.builder {
                        setId(id)
                        setContent(content ?: "ERROR: content is null")
                        setIsFav(isFav == 1)
                        setTitle(title ?: "ERROR: title is null")
                        setTime(time ?: 0)
                        json.decodeFromString<List<String>?>(attachments ?: "[]")?.let {
                            setAttachments(it)
                        }
                        setType(type ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(feelings)
    }
}
