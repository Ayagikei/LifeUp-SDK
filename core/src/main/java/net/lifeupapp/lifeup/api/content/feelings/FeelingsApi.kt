package net.lifeupapp.lifeup.api.content.feelings

import android.content.Context
import android.net.Uri
import kotlinx.serialization.decodeFromString
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull
import net.lifeupapp.lifeup.api.utils.json

class FeelingsApi(private val context: Context) : ContentProviderApi {

    fun listFeelings(offset: Int = 0, limit: Int = 100): Result<List<Feelings>> {
        val feelings = mutableListOf<Feelings>()
        try {
            val uri = Uri.parse(ContentProviderUrl.FEELINGS).buildUpon()
                .appendQueryParameter("offset", offset.toString())
                .appendQueryParameter("limit", limit.toString())
                .build()

            context.forEachContent(uri.toString()) {
                val id = it.getLongOrNull("_ID")
                val content = it.getStringOrNull("content")
                val isFav = it.getIntOrNull("isFav")
                val title = it.getStringOrNull("title")
                val time = it.getLongOrNull("time")
                val attachments = it.getStringOrNull("attachments")
                val type = it.getIntOrNull("type")

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