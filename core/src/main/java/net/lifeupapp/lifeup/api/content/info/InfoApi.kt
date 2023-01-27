package net.lifeupapp.lifeup.api.content.info

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent

class InfoApi(private val context: Context) : ContentProviderApi {

    fun getInfo(): Result<Info> {
        try {
            context.forEachContent(ContentProviderUrl.INFO) {
                val appVersion = it.getIntOrNull(0)
                val appVersionName = it.getStringOrNull(1)
                val apiVersion = it.getIntOrNull(2)

                return Result.success(Info.builder {
                    setAppVersion(appVersion ?: 0)
                    setAppVersionName(appVersionName ?: "")
                    setApiVersion(apiVersion ?: 0)
                })
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.failure(IllegalAccessException())
    }

}