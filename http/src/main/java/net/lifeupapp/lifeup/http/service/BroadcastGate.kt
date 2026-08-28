package net.lifeupapp.lifeup.http.service

import android.os.SystemClock
import net.lifeupapp.lifeup.api.LifeUpApi

object BroadcastGate {
    enum class Status { Unknown, On, Off, Unsupported }

    @Volatile
    var status: Status = Status.Unknown
        private set

    @Volatile
    private var at = 0L

    private const val TTL_MS = 5_000L
    private val lock = Any()

    fun cached(): Boolean? = when (status) {
        Status.On -> true
        Status.Off -> false
        else -> null
    }

    fun refresh(force: Boolean = false): Boolean? = synchronized(lock) {
        if (status == Status.Unsupported) return null
        val now = SystemClock.elapsedRealtime()
        if (!force && at != 0L && now - at < TTL_MS) return cached()
        query()
        cached()
    }

    fun enable(): Boolean = synchronized(lock) {
        val bundle = LifeUpApi.callApiWithContentProvider("app_settings", "broadcast_event=true")
        val ok = bundle != null && bundle.getBoolean("api_result", true) &&
            bundle.getString("error_code").isNullOrEmpty()
        if (ok) {
            status = Status.On
            at = SystemClock.elapsedRealtime()
        } else {
            query()
        }
        status == Status.On
    }

    private fun query() {
        try {
            val bundle = LifeUpApi.callApiWithContentProvider("query", "key=broadcast")
            at = SystemClock.elapsedRealtime()
            if (bundle == null) {
                status = Status.Unknown
                return
            }
            val error = bundle.getString("error_code")
            if (error == "unsupported_parameter") {
                status = Status.Unsupported
                return
            }
            if (bundle.getBoolean("api_result", true).not() || !bundle.containsKey("enabled")) {
                status = Status.Unknown
                return
            }
            status = if (bundle.getBoolean("enabled")) Status.On else Status.Off
        } catch (_: Exception) {
            status = Status.Unknown
            at = SystemClock.elapsedRealtime()
        }
    }
}
