package app.grapheneos.deskclock.alarm.util

object AlarmConstants {
    const val CHANNEL_ID = "ALARM_CHANNEL"
    const val CHANNEL_NAME = "Alarm"
    const val NOTIFICATION_ID = 1001

    const val ACTION_FIRE_ALARM = "ALARM_FIRED"
    const val EXTRA_INSTANCE_ID = "INSTANCE_ID"

    const val PM_TAG = "DeskClock:AlarmService"
    const val WAKE_LOCK_TIMEOUT = 10 * 60 * 1000L
}
