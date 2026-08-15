package app.grapheneos.deskclock.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import app.grapheneos.deskclock.core.util.Constants

class ChannelManager(private val context: Context) {
    private val nm = context.getSystemService(NotificationManager::class.java)

    fun createAllChannels() {
        val alarmChannel = NotificationChannel(
            Constants.Notifications.Alarm.CHANNEL_ID,
            Constants.Notifications.Alarm.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val timerChannel = NotificationChannel(
            Constants.Notifications.Timer.CHANNEL_ID,
            Constants.Notifications.Timer.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val stopwatchChannel = NotificationChannel(
            Constants.Notifications.Stopwatch.CHANNEL_ID,
            Constants.Notifications.Stopwatch.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        nm?.createNotificationChannels(listOf(alarmChannel, timerChannel, stopwatchChannel))
    }
}
