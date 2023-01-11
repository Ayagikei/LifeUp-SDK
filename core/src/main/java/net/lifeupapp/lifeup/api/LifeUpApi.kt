package net.lifeupapp.lifeup.api

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import net.lifeupapp.lifeup.api.Val.LIFEUP_PACKAGE_NAME
import net.lifeupapp.lifeup.api.content.tasks.TasksApi
import net.lifeupapp.lifeup.api.utils.isAppInstalled

@SuppressLint("StaticFieldLeak")
object LifeUpApi : LifeUpApiDef {

    private lateinit var appCtx: Context

    // FIXME: better init implementation, only for testing now
    lateinit var tasksApi: TasksApi


    override fun init(context: Context) {
        appCtx = context.applicationContext ?: context
        tasksApi = TasksApi(appCtx)
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
