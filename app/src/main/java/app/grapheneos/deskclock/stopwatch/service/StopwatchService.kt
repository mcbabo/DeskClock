package app.grapheneos.deskclock.stopwatch.service

import android.Manifest
import android.app.Notification
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
import app.grapheneos.deskclock.core.util.Intents
import app.grapheneos.deskclock.stopwatch.data.StopwatchData
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import app.grapheneos.deskclock.stopwatch.util.StopwatchPrecision
import app.grapheneos.deskclock.stopwatch.util.formatStopwatchTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service that keeps the stopwatch running and updates its persistent notification.
 */
class StopwatchService : BaseAlertService(Constants.Stopwatch.PM_TAG) {
    private val stopwatchRepository: StopwatchRepository by inject()
    private var observationJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeStopwatch()
        return START_STICKY
    }

    private fun observeStopwatch() {
        if (observationJob != null) return
        observationJob = serviceScope.launch {
            combine(
                stopwatchRepository.state,
                stopwatchRepository.elapsedMillis
            ) { state, elapsed ->
                state to (elapsed / 1000)
            }
                .distinctUntilChanged()
                .collect { (state, _) ->
                    val isActive = state.isRunning || state.accumulatedMillis > 0
                    if (isActive) {
                        showNotification()
                    } else {
                        stopSelf()
                    }
                }
        }
    }

    private fun showNotification() {
        val state = stopwatchRepository.state.value
        val notification = buildStopwatchNotification(state)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(
                Constants.Notifications.Stopwatch.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            Log.e("StopwatchService", "Notification permission NOT granted")
        }
    }

    private fun buildStopwatchNotification(state: StopwatchData): Notification {
        val elapsed = state.getElapsedMillis()
        val formattedTime = formatStopwatchTime(elapsed, StopwatchPrecision.NONE)

        val builder = baseNotificationBuilder(
            channelId = Constants.Notifications.Stopwatch.CHANNEL_ID,
            icon = R.drawable.ic_timer
        )
            .setContentTitle(getString(R.string.tab_stopwatch))
            .setContentText(formattedTime)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .addAction(buildStartPauseAction(state.isRunning))
            .addAction(
                if (state.isRunning) {
                    android.R.drawable.ic_input_add
                } else {
                    android.R.drawable.ic_menu_close_clear_cancel
                },
                getString(
                    if (state.isRunning) {
                        R.string.lap
                    } else {
                        R.string.reset
                    }
                ),
                Intents.Stopwatch.createStopwatchReceiverPendingIntent(
                    this,
                    Constants.Stopwatch.ACTION_LAP_RESET
                )
            )

        if (state.isRunning) {
            builder.setUsesChronometer(true)
            builder.setWhen(System.currentTimeMillis() - elapsed)
            builder.setShowWhen(true)
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
        }

        return builder.build()
    }

    private fun buildStartPauseAction(isRunning: Boolean): NotificationCompat.Action {
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
            Intents.Stopwatch.createStopwatchReceiverPendingIntent(
                this,
                Constants.Stopwatch.ACTION_START_PAUSE
            )
        )
    }
}
