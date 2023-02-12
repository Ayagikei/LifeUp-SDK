package net.lifeupapp.lifeup.api

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.api.Val.LIFEUP_PACKAGE_NAME
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.achievements.AchievementApi
import net.lifeupapp.lifeup.api.content.feelings.FeelingsApi
import net.lifeupapp.lifeup.api.content.info.InfoApi
import net.lifeupapp.lifeup.api.content.shop.ItemsApi
import net.lifeupapp.lifeup.api.content.skills.SkillsApi
import net.lifeupapp.lifeup.api.content.tasks.TasksApi
import net.lifeupapp.lifeup.api.utils.isAppInstalled

@SuppressLint("StaticFieldLeak")
object LifeUpApi : LifeUpApiDef {

    private lateinit var appCtx: Context

    private var contentProviderApis = emptyList<ContentProviderApi>()

    override fun init(context: Context) {
        appCtx = context.applicationContext ?: context
        contentProviderApis = buildContentProviderApis()
    }

    private fun buildContentProviderApis(): List<ContentProviderApi> {
        return listOf(
            TasksApi(appCtx),
            AchievementApi(appCtx),
            ItemsApi(appCtx),
            SkillsApi(appCtx),
            InfoApi(appCtx),
            FeelingsApi(appCtx)
        )
    }


    override fun isLifeUpInstalled(): Boolean {
        return isAppInstalled(appCtx, LIFEUP_PACKAGE_NAME)
    }


    override fun call(context: Context?, url: String) {
        startApiActivity(context, url)
    }

    override fun requestContentProviderPermission(appName: String) {
        startApiActivity(
            appCtx,
            "lifeup://api/request_permission?request_content_provider=true&app_name=${appName}&package_name=${appCtx.packageName}"
        )
    }


    override fun startApiActivity(context: Context?, url: String) {
        val action = parseUriIntent(url)
        action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        (context ?: appCtx).startActivity(action)
    }

    override fun startApiActivityWithResult(activity: Activity, url: String, requestCode: Int) {
        val action = parseUriIntent(url)
        action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivityForResult(action, requestCode)
    }

    override fun callApiWithContentProvider(method: String, arg: String): Bundle? {
        return appCtx.contentResolver.call(
            Uri.parse("content://net.sarasarasa.lifeup.provider.api/"), method, arg, null
        )
    }

    override fun callApiWithContentProvider(url: String): Bundle? {
        return callApiWithContentProvider(
            url.substringBefore("?").replace("lifeup://api/", ""), url.substringAfter("?")
        )
    }

    override fun <T : ContentProviderApi> getContentProviderApi(clazz: Class<T>): T {
        return contentProviderApis.first { it.javaClass == clazz } as T
    }

    inline fun <reified T : ContentProviderApi> getContentProviderApi(): T {
        return getContentProviderApi(T::class.java)
    }

    @Throws
    private fun parseUriIntent(uriString: String): Intent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent.parseUri(uriString, Intent.URI_ALLOW_UNSAFE)
        } else {
            Intent.parseUri(uriString, 0)
        }
        // forbid launching activities without BROWSABLE category
        intent.addCategory("android.intent.category.BROWSABLE")
        // forbid explicit call
        intent.component = null
        // forbid intent with selector intent
        intent.selector = null
        return intent
    }
}

internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}