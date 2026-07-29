package app.grapheneos.deskclock.core.notification

object NotificationConstants {
    const val WAKE_LOCK_TIMEOUT = 10 * 60 * 1000L

    object Alarm {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val CHANNEL_NAME = "Alarm"
        const val NOTIFICATION_ID = 1001
    }

    object Timer {
        const val CHANNEL_ID = "TIMER_CHANNEL"
        const val CHANNEL_NAME = "Timer"
        const val NOTIFICATION_ID = 2001
    }

    object Stopwatch {
        const val CHANNEL_ID = "STOPWATCH_CHANNEL"
        const val CHANNEL_NAME = "Stopwatch"
        const val NOTIFICATION_ID = 3001
    }
}
