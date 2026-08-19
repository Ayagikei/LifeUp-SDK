package net.lifeupapp.lifeup.http.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle

object EventHub {
    val actions = listOf(
        "app.lifeup.task.complete",
        "app.lifeup.task.overdue",
        "app.lifeup.task.giveup",
        "app.lifeup.achievement.unlock",
        "app.lifeup.achievement.condition.unlock",
        "app.lifeup.item.purchase",
        "app.lifeup.item.reward",
        "app.lifeup.item.use",
        "app.lifeup.synthesis.complete",
        "app.lifeup.pomodoro.start",
        "app.lifeup.pomodoro.pause",
        "app.lifeup.pomodoro.complete",
        "app.lifeup.pomodoro.stop",
        "app.lifeup.feelings.add",
        "app.lifeup.level.up",
        "app.lifeup.level.down",
        "app.lifeup.item.countdown.start",
        "app.lifeup.item.countdown.complete",
        "app.lifeup.item.countdown.stop",
        "app.lifeup.timing.start",
        "app.lifeup.timing.complete",
        "app.lifeup.timing.pause",
        "app.lifeup.timing.abandon",
    )

    private val buffer = EventBuffer()
    private val listeners = mutableListOf<(CloudEvent) -> Unit>()
    private var receiver: BroadcastReceiver? = null

    val latestId: Long get() = buffer.latestId

    fun start(context: Context) {
        if (receiver != null) return
        val filter = IntentFilter().apply { actions.forEach(::addAction) }
        val next = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                publish(action, extrasOf(intent.extras))
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            context.applicationContext.registerReceiver(next, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.applicationContext.registerReceiver(next, filter)
        }
        receiver = next
    }

    fun stop(context: Context) {
        receiver?.let {
            runCatching { context.applicationContext.unregisterReceiver(it) }
        }
        receiver = null
        synchronized(listeners) { listeners.clear() }
    }

    fun publish(action: String, extras: Map<String, String>): CloudEvent {
        val event = buffer.add(action, extras)
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { listener -> runCatching { listener(event) } }
        return event
    }

    fun since(after: Long, limit: Int = EventBuffer.CAPACITY): List<CloudEvent> = buffer.since(after, limit)

    fun addListener(listener: (CloudEvent) -> Unit) {
        synchronized(listeners) { listeners.add(listener) }
    }

    fun removeListener(listener: (CloudEvent) -> Unit) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    private fun extrasOf(bundle: Bundle?): Map<String, String> {
        if (bundle == null) return emptyMap()
        return bundle.keySet().associateWith { key -> bundle.get(key)?.toString().orEmpty() }
    }
}
