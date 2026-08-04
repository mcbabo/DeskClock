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

    const val BIT_SUN = 1 shl 0
    const val BIT_MON = 1 shl 1
    const val BIT_TUE = 1 shl 2
    const val BIT_WED = 1 shl 3
    const val BIT_THU = 1 shl 4
    const val BIT_FRI = 1 shl 5
    const val BIT_SAT = 1 shl 6

    const val BIT_ALL_DAYS = 0x7F
    const val BIT_WEEKDAYS = BIT_MON or BIT_TUE or BIT_WED or BIT_THU or BIT_FRI
    const val BIT_WEEKEND = BIT_SAT or BIT_SUN
}
