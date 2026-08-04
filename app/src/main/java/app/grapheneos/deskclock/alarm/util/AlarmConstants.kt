package app.grapheneos.deskclock.alarm.util

object AlarmConstants {
    const val ACTION_FIRE_ALARM = "ALARM_FIRED"
    const val EXTRA_INSTANCE_ID = "INSTANCE_ID"
    const val EXTRA_ALARM_LABEL = "ALARM_LABEL"
    const val EXTRA_ALARM_HOUR = "ALARM_HOUR"
    const val EXTRA_ALARM_MINUTE = "ALARM_MINUTE"
    const val EXTRA_ALARM_RINGTONE_URI = "ALARM_RINGTONE_URI"
    const val EXTRA_ALARM_VIBRATE = "ALARM_VIBRATE"

    const val PM_TAG = "DeskClock:AlarmService"
    const val DEFAULT_SNOOZE_TIME = 5
    const val WAVE_FORM = 1000L
}
