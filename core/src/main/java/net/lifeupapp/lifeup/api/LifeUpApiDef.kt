package net.lifeupapp.lifeup.api

import android.app.Activity
import android.content.Context
import android.os.Bundle
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
        "use startApiActivity instead",
        ReplaceWith(
            "startApiActivity(url)", "net.lifeupapp.lifeup.api.LifeUpApi.startApiActivity"
        )
    )
    fun call(context: Context?, url: String)


    /**
     * start the [url] in LifeUp
     *
     * Note that if you call the method in the background. Your application may require permission to launch applications. For more information, please refer to: https://developer.android.com/guide/components/activities/background-starts
     * Some mobile phone manufacturers may also add additional restrictions.
     * Therefore we recommend that you use the content provider to call the API, check [callApiWithContentProvider].
     *
     * @param context the context to start the activity
     * @param url the url to start, which should be in the format of lifeup://api/xxx
     */
    fun startApiActivity(context: Context?, url: String)

    fun startApiActivityWithResult(activity: Activity, url: String, requestCode: Int)


    /**
     * before using the content provider, you need to request the permission
     *
     * @param appName the name of your app, which will be shown in the permission dialog
     */
    fun requestContentProviderPermission(appName: String)


    /**
     * call the [url] in LifeUp, and return the result in [Bundle]
     *
     * for this method to work, you need to request permission and obtain the permission from user first
     * please check the [requestContentProviderPermission] method
     *
     * @param url the url to call, which should be in the format of lifeup://api/xxx
     */
    fun callApiWithContentProvider(url: String): Bundle?


    /**
     * call the [method] in LifeUp, and return the result in [Bundle]
     *
     * same as the [callApiWithContentProvider] method, but you can specify the [method] and [arg] directly
     *
     * @param method the method to call, for example, "query"
     * @param arg the argument to pass to the method, for example, "key=coin"
     */
    fun callApiWithContentProvider(method: String, arg: String): Bundle?


    /**
     * get the api for the specific [ContentProviderApi], and then you can get detailed information from LifeUp
     * for example, you can get the list of tasks, achievements, items, skills, etc.
     *
     * for this method to work, you need to request permission and obtain the permission from user first
     * please check the [requestContentProviderPermission] method
     *
     * @param clazz the class of the [ContentProviderApi] you want to get
     * @return the api for the specific [ContentProviderApi]
     *
     * for example, you can call it like this to get the api for the items:
     * LifeUpApi.getContentProviderApi<ItemsApi>()
     *
     * currently, we have the following apis:
     * [net.lifeupapp.lifeup.api.content.achievements.AchievementApi]
     * [net.lifeupapp.lifeup.api.content.feelings.FeelingsApi]
     * [net.lifeupapp.lifeup.api.content.info.InfoApi]
     * [net.lifeupapp.lifeup.api.content.shop.ItemsApi]
     * [net.lifeupapp.lifeup.api.content.skills.SkillsApi]
     * [net.lifeupapp.lifeup.api.content.tasks.TasksApi]
     */
    fun <T : ContentProviderApi> getContentProviderApi(clazz: Class<T>): T
}
