package app.grapheneos.deskclock.core.util

import android.app.Activity
import android.app.KeyguardManager
import android.view.WindowManager.LayoutParams

fun Activity.configureShowOnLockScreen() {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    window.addFlags(
        LayoutParams.FLAG_KEEP_SCREEN_ON or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )
    val keyguardManager = getSystemService(KeyguardManager::class.java)
    keyguardManager?.requestDismissKeyguard(this, null)
}
