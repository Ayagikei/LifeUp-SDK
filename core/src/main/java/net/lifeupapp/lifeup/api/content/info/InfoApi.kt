package net.lifeupapp.lifeup.api.content.info

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class InfoApi(private val context: Context) : ContentProviderApi {

    fun getInfo(): Result<Info> {
        try {
            context.forEachContent(ContentProviderUrl.INFO) {
                val appVersion = it.getIntOrNull("appVersion")
                val appVersionName = it.getStringOrNull("appVersionName")
                val apiVersion = it.getIntOrNull("apiVersion")

                return Result.success(
                    Info.builder {
                        setAppVersion(appVersion ?: 0)
                        setAppVersionName(appVersionName ?: "")
                        setApiVersion(apiVersion ?: 0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.failure(IllegalAccessException())
    }
}