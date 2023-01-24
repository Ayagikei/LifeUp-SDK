package net.lifeupapp.lifeup.api

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import net.lifeupapp.lifeup.api.Val.LIFEUP_PACKAGE_NAME
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.achievements.AchievementApi
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
            SkillsApi(appCtx)
        )
    }


    override fun isLifeUpInstalled(): Boolean {
        return isAppInstalled(appCtx, LIFEUP_PACKAGE_NAME)
    }


    override fun call(context: Context?, url: String) {
        startApiActivity(url)
    }


    override fun startApiActivity(url: String) {
        val action = parseUriIntent(url)
        action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appCtx.startActivity(action)
    }

    override fun startApiActivityWithResult(activity: Activity, url: String, requestCode: Int) {
        val action = parseUriIntent(url)
        action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivityForResult(action, requestCode)
    }

    override fun startApiWithContentProvider(method: String, arg: String): Bundle? {
        return appCtx.contentResolver.call(
            Uri.parse("content://net.sarasarasa.lifeup.provider.api/"),
            method,
            arg,
            null
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
