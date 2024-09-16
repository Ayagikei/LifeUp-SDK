package net.lifeupapp.lifeup.api.content.data

import android.content.Context
import android.net.Uri
import android.os.Bundle
import net.lifeupapp.lifeup.api.LifeUpApi
import net.lifeupapp.lifeup.api.content.CommonApi
import net.lifeupapp.lifeup.api.content.ContentProviderApi

class DataApi(private val context: Context) : ContentProviderApi {

    fun exportBackup(withMedia: Boolean = true): Result<Bundle?> {
        try {
            val uri = Uri.parse(CommonApi.EXPORT_BACKUP).buildUpon()
                .appendQueryParameter("withMedia", withMedia.toString())
                .build()

            val bundle = LifeUpApi.callApiWithContentProvider(uri.toString())
            return if (bundle != null) {
                Result.success(bundle)
            } else {
                Result.failure(IllegalStateException("Backup file URI not found"))
            }

        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}