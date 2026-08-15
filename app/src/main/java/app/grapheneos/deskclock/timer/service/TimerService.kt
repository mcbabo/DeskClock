package app.grapheneos.deskclock.timer.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.notification.baseNotificationBuilder
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.timer.data.TimerData
import app.grapheneos.deskclock.timer.data.TimerReceiver
import app.grapheneos.deskclock.timer.data.TimerRepository
import app.grapheneos.deskclock.timer.presentation.popup.TimerPopUpActivity
import app.grapheneos.deskclock.timer.util.TimerUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TimerService : BaseAlertService(Constants.Timer.PM_TAG) {
    private val timerRepository: TimerRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private var isSoundPlaying = false
    private var observationJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeTimer()
        return START_STICKY
    }

    private fun observeTimer() {
        if (observationJob != null) return
        observationJob = serviceScope.launch {
            combine(timerRepository.state, timerRepository.remainingMillis) { state, remaining ->
                state to (remaining / 1000)
            }
                .distinctUntilChanged()
                .collect { (state, _) ->
                    val remaining = state.getRemainingTime()

                    if (state.isStarted && remaining > 0) {
                        showNotification(state)
                    } else if (state.isFinished) {
                        if (!isSoundPlaying) {
                            isSoundPlaying = true
                            launchPopUp()
                            val appSettings = settingsRepository.settings.first()
                            val ringtoneVolume = if (appSettings.useCustomRingtoneVolume) {
                                appSettings.ringtoneVolume
                            } else {
                                null
                            }
                            startAlert(
                                ringtoneVolume = ringtoneVolume,
                                graduallyIncreaseVolume = appSettings.graduallyIncreaseVolume,
                                graduallyIncreaseVolumeDuration = appSettings.graduallyIncreaseVolumeDuration
                            )
                        }
                        showNotification(state)
                    } else {
                        stopSelf()
                    }
                }
        }
    }

    private fun launchPopUp() {
        val intent = Intent(this, TimerPopUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun showNotification(state: TimerData) {
        val notification = buildTimerNotification(state)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(
                Constants.Notifications.Timer.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            Log.e(Constants.TAG_TIMER_SERVICE, "Notification permission NOT granted")
        }
    }

    private fun buildTimerNotification(state: TimerData): Notification {
        val remaining = state.getRemainingTime()
        val formattedTime = TimerUtils.formatRemainingTime(remaining)

        return baseNotificationBuilder(
            channelId = Constants.Notifications.Timer.CHANNEL_ID,
            icon = R.drawable.ic_hourglass
        )
            .setContentTitle(getString(R.string.timer))
            .setContentText(formattedTime)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .addAction(buildPauseAction(state.isRunning))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.reset),
                getPendingIntent(Constants.Timer.ACTION_RESET)
            )
            .build()
    }

    private fun buildPauseAction(isRunning: Boolean): NotificationCompat.Action {
        val icon = if (isRunning) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val title = if (isRunning) {
            getString(R.string.pause)
        } else {
            getString(R.string.resume)
        }
        return NotificationCompat.Action(
            icon,
            title,
            getPendingIntent(Constants.Timer.ACTION_PAUSE_RESUME)
        )
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent().apply {
            component = ComponentName(this@TimerService, TimerReceiver::class.java)
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
