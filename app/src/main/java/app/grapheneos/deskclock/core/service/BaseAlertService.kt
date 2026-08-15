package app.grapheneos.deskclock.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.audio.VibrationManager
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.acquireWakeLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

/**
 * A base class for foreground services that handle critical alerts (Alarms, Timers).
 * Manages common concerns like wake locks, audio focus, and alert playback.
 */
abstract class BaseAlertService(private val tag: String) : Service() {
    protected var wakeLock: PowerManager.WakeLock? = null
    protected val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    protected val audioPlayer: AudioPlayer by inject()
    protected val vibrationManager: VibrationManager by inject()

    override fun onCreate() {
        super.onCreate()
        wakeLock = acquireWakeLock(
            this,
            tag,
            Constants.Notifications.WAKE_LOCK_TIMEOUT
        )
    }

    protected fun startAlert(
        ringtoneUri: String? = null,
        vibrate: Boolean = true,
        ringtoneVolume: Float? = null,
        graduallyIncreaseVolume: Boolean = false,
        graduallyIncreaseVolumeDuration: Int = 30
    ) {
        audioPlayer.playAlarm(
            ringtoneUri,
            ringtoneVolume = ringtoneVolume,
            graduallyIncreaseVolume = graduallyIncreaseVolume,
            graduallyIncreaseVolumeDuration = graduallyIncreaseVolumeDuration
        )
        if (vibrate) {
            vibrationManager.startAlarmVibration()
        }
    }

    override fun onDestroy() {
        audioPlayer.stop()
        vibrationManager.stop()
        serviceScope.cancel()
        wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
