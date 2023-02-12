package net.lifeupapp.lifeup.http.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import net.lifeupapp.lifeup.api.LifeUpApi
import net.lifeupapp.lifeup.http.service.notification.NotificationChannels

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        appCtx = this
        runStartupTasks(this)
    }

    private fun runStartupTasks(context: Context) {
        // 1. create the notification channels
        NotificationChannels.createChannels(context)
        // 2. init lifeup sdk
        LifeUpApi.init(context)
    }
}

lateinit var app: Application
    private set

@SuppressLint("StaticFieldLeak")
lateinit var appCtx: Context
    private set
