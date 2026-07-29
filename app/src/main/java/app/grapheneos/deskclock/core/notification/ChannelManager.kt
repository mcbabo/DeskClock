package app.grapheneos.deskclock.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class ChannelManager(private val context: Context) {
    private val nm = context.getSystemService(NotificationManager::class.java)

    fun createAllChannels() {
        val alarmChannel = NotificationChannel(
            NotificationConstants.Alarm.CHANNEL_ID,
            NotificationConstants.Alarm.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val timerChannel = NotificationChannel(
            NotificationConstants.Timer.CHANNEL_ID,
            NotificationConstants.Timer.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val stopwatchChannel = NotificationChannel(
            NotificationConstants.Stopwatch.CHANNEL_ID,
            NotificationConstants.Stopwatch.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        nm?.createNotificationChannels(listOf(alarmChannel, timerChannel, stopwatchChannel))
    }
}
