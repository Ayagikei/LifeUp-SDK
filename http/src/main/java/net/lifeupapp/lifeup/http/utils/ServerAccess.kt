package net.lifeupapp.lifeup.http.utils

object CloudControl {
    const val SCHEME = "lifeupcloud"
    const val HOST_START = "start"
    const val HOST_STOP = "stop"
    const val URL_START = "$SCHEME://$HOST_START"
    const val URL_STOP = "$SCHEME://$HOST_STOP"

    enum class Action { START, STOP }

    fun parse(scheme: String?, host: String?): Action? {
        if (!SCHEME.equals(scheme, ignoreCase = true)) return null
        return when (host?.lowercase()) {
            HOST_START -> Action.START
            HOST_STOP -> Action.STOP
            else -> null
        }
    }
}
