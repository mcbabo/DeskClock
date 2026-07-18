package app.grapheneos.deskclock.core.util

import android.content.Context
import android.os.PowerManager

object ServiceUtils {
    fun acquireWakeLock(context: Context, tag: String, timeout: Long): PowerManager.WakeLock {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
            acquire(timeout)
        }
    }
}
