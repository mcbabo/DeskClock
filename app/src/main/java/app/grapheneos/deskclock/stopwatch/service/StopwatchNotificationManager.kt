package app.grapheneos.deskclock.stopwatch.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.stopwatch.data.StopwatchData
import app.grapheneos.deskclock.stopwatch.data.StopwatchReceiver
import app.grapheneos.deskclock.stopwatch.util.StopwatchConstants
import app.grapheneos.deskclock.stopwatch.util.StopwatchPrecision
import app.grapheneos.deskclock.stopwatch.util.formatStopwatchTime

class StopwatchNotificationManager(private val context: Context) {
    fun buildNotification(state: StopwatchData): Notification {
        val elapsed = state.getElapsedMillis()
        val formattedTime = formatStopwatchTime(elapsed, StopwatchPrecision.NONE)

        val builder =
            NotificationCompat.Builder(context, NotificationConstants.Stopwatch.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_stopwatch)
                .setContentTitle(context.getString(R.string.tab_stopwatch))
                .setContentText(formattedTime)
                .setOngoing(state.isRunning)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(buildStartPauseAction(state.isRunning))
                .addAction(
                    if (state.isRunning) {
                        android.R.drawable.ic_input_add
                    } else {
                        android.R.drawable.ic_menu_close_clear_cancel
                    },
                    context.getString(
                        if (state.isRunning) {
                            R.string.lap
                        } else {
                            R.string.reset
                        }
                    ),
                    getPendingIntent(StopwatchConstants.ACTION_LAP_RESET)
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
            context.getString(R.string.pause)
        } else {
            context.getString(R.string.resume)
        }
        return NotificationCompat.Action(
            icon,
            title,
            getPendingIntent(StopwatchConstants.ACTION_START_PAUSE)
        )
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, StopwatchReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
