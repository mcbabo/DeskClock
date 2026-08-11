package app.grapheneos.deskclock.core.util

object Constants {
    const val METADATA_PERMISSIONS_REQUIRED = "permissions_required"
    const val ATTRIBUTION_TAG_DESKCLOCK_SERVICE = "deskclock_service"
    const val DATABASE_NAME = "deskclock.db"
    const val SETTINGS_DATASTORE_NAME = "app_settings"
    const val SETTINGS_DATASTORE_KEY = "settings_json"

    const val TAG_AUDIO_PLAYER = "AudioPlayer"

    const val SCHEME_PACKAGE = "package:"

    const val COMPOSE_LABEL_THUMB_COLOR = "thumbColor"
    const val COMPOSE_LABEL_THEME_TRANSITION = "ThemeTransition"

    object Alarm {
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

    object Timer {
        const val ACTION_PAUSE_RESUME = "app.grapheneos.deskclock.TIMER_PAUSE_RESUME"
        const val ACTION_RESET = "app.grapheneos.deskclock.TIMER_RESET"
        const val PM_TAG = "DeskClock:TimerService"
    }

    object Stopwatch {
        const val ACTION_START_PAUSE = "app.grapheneos.deskclock.STOPWATCH_START_PAUSE"
        const val ACTION_LAP_RESET = "app.grapheneos.deskclock.STOPWATCH_LAP_RESET"
        const val PM_TAG = "DeskClock:StopwatchService"
    }

    object Actions {
        const val ADD_ALARM = "app.grapheneos.deskclock.ACTION_ADD_ALARM"
        const val ADD_CLOCK = "app.grapheneos.deskclock.ACTION_ADD_CLOCK"
        const val START_TIMER = "app.grapheneos.deskclock.ACTION_START_TIMER"
        const val START_STOPWATCH = "app.grapheneos.deskclock.ACTION_START_STOPWATCH"
    }
}
