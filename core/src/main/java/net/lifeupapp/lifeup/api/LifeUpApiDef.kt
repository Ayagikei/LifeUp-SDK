package net.lifeupapp.lifeup.api

import android.app.Activity
import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi

interface LifeUpApiDef {
    /**
     * init the library with your [context]
     */
    fun init(context: Context)

    /**
     * check if LifeUp is installed
     *
     * @return true if LifeUp is installed
     */
    fun isLifeUpInstalled(): Boolean

    /**
     * call lifeup to open the [uri], which will not return any result
     */
    @Deprecated(
        "use startApiActivity instead", ReplaceWith(
            "startApiActivity(url)", "net.lifeupapp.lifeup.api.LifeUpApi.startApiActivity"
        )
    )
    fun call(context: Context?, url: String)

    fun startApiActivity(url: String)

    fun startApiActivityWithResult(activity: Activity, url: String, requestCode: Int)

    fun startApiWithContentProvider(url: String)

    fun <T : ContentProviderApi> getContentProviderApi(clazz: Class<T>): T
}