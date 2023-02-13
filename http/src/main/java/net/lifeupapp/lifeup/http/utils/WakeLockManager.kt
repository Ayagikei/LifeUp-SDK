package net.lifeupapp.lifeup.http.utils

import android.os.Build
import android.os.PowerManager
import net.lifeupapp.lifeup.http.base.appCtx
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


class WakeLockManager(
    tag: String
) {

    private val wakeLock: PowerManager.WakeLock? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appCtx.getSystemService(PowerManager::class.java)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LifeUp_Cloud:$tag").also {
                    it.setReferenceCounted(false)
                }
        } else {
            null
        }

    fun stayAwake(timeout: Long = 10.minutes.toLong(DurationUnit.MILLISECONDS)) {
        wakeLock?.acquire(timeout)
    }

    fun release() {
        wakeLock?.release()
    }
}
