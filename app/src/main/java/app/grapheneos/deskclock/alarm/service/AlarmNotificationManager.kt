package app.grapheneos.deskclock.alarm.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpActivity
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.core.notification.NotificationConstants

class AlarmNotificationManager(private val context: Context) {
    @SuppressLint("FullScreenIntentPolicy")
    fun buildAlarmNotification(
        instanceId: Long,
        label: String = "",
        hour: Int = -1,
        minute: Int = -1
    ): Notification {
        val fullScreenIntent = getFullScreenIntent(instanceId, label, hour, minute)
        return NotificationCompat.Builder(context, NotificationConstants.Alarm.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label.ifEmpty { context.getString(R.string.alarm) })
            .setContentText(context.getString(R.string.ringing_dots))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, true)
            .setContentIntent(fullScreenIntent)
            .setOngoing(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setLocalOnly(true)
            .build()
    }

    fun updateNotification(notificationId: Int, notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    private fun getFullScreenIntent(
        id: Long,
        label: String,
        hour: Int,
        minute: Int
    ): PendingIntent {
        val intent = Intent().apply {
            component = ComponentName(context, AlarmPopUpActivity::class.java)
            putExtra(AlarmConstants.EXTRA_INSTANCE_ID, id)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmConstants.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmConstants.EXTRA_ALARM_MINUTE, minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
