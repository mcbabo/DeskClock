package app.grapheneos.deskclock.core.util

import android.app.Activity
import android.app.KeyguardManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.text.format.DateFormat
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.grapheneos.deskclock.stopwatch.service.StopwatchService
import app.grapheneos.deskclock.timer.service.TimerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Activity.configureShowOnLockScreen() {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    window.addFlags(
        LayoutParams.FLAG_KEEP_SCREEN_ON or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )
    val keyguardManager = getSystemService(KeyguardManager::class.java)
    keyguardManager?.requestDismissKeyguard(this, null)
}

/**
 * Collects the given one-off [effect] flow only while the activity is at least in the
 * [Lifecycle.State.STARTED] state, forwarding every emission to [onEffect].
 */
fun <T> ComponentActivity.collectEffectsOnStart(effect: Flow<T>, onEffect: (T) -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effect.collect(onEffect)
        }
    }
}

/**
 * Stops the given background [serviceClass] and finishes this activity, a common pattern used
 * by alert pop-up screens once the user dismisses or stops the alarm/timer.
 */
fun ComponentActivity.stopServiceAndFinish(serviceClass: Class<out Service>) {
    stopService(Intent(this, serviceClass))
    finish()
}

fun acquireWakeLock(context: Context, tag: String, timeout: Long): PowerManager.WakeLock {
    val pm = context.getSystemService(PowerManager::class.java)
    return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
        acquire(timeout)
    }
}

fun formatSystemTime(context: Context, hour: Int, minute: Int): String {
    val localTime = LocalTime.of(hour, minute)
    val locale = Locale.getDefault()

    val is24Hour = DateFormat.is24HourFormat(context)
    val skeleton = if (is24Hour) "Hm" else "hm"

    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)

    return localTime.format(formatter)
}

fun Context.startTimerService() {
    startForegroundService(Intent(this, TimerService::class.java))
}

fun Context.stopTimerService() {
    stopService(Intent(this, TimerService::class.java))
}

fun Context.startStopwatchService() {
    startForegroundService(Intent(this, StopwatchService::class.java))
}

fun Context.stopStopwatchService() {
    stopService(Intent(this, StopwatchService::class.java))
}
