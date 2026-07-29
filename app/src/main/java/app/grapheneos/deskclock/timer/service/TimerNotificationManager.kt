package app.grapheneos.deskclock.timer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.timer.data.TimerData
import app.grapheneos.deskclock.timer.data.TimerReceiver
import app.grapheneos.deskclock.timer.util.TimerConstants
import app.grapheneos.deskclock.timer.util.TimerUtils

class TimerNotificationManager(private val context: Context) {
    fun buildTimerNotification(state: TimerData): Notification {
        val remaining = state.getRemainingTime()
        val formattedTime = TimerUtils.formatRemainingTime(remaining)

        return NotificationCompat.Builder(context, NotificationConstants.Timer.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setContentTitle(context.getString(R.string.timer))
            .setContentText(formattedTime)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(buildPauseAction(state.isRunning))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.reset),
                getPendingIntent(TimerConstants.ACTION_RESET)
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
            context.getString(R.string.pause)
        } else {
            context.getString(R.string.resume)
        }
        return NotificationCompat.Action(
            icon,
            title,
            getPendingIntent(TimerConstants.ACTION_PAUSE_RESUME)
        )
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
