package app.grapheneos.deskclock.core.util

object Constants {
    const val METADATA_PERMISSIONS_REQUIRED = "permissions_required"
    const val ATTRIBUTION_TAG_DESKCLOCK_SERVICE = "deskclock_service"
    const val DATABASE_NAME = "deskclock.db"
    const val DEVICE_PROTECTED_CONTEXT = "DeviceProtectedContext"
    const val ATTRIBUTED_CONTEXT = "AttributedContext"
    const val SETTINGS_DATASTORE_NAME = "app_settings"
    const val SETTINGS_DATASTORE_KEY = "settings_json"

    const val TAG_APPLICATION = "DeskClockApp"
    const val TAG_AUDIO_PLAYER = "AudioPlayer"
    const val TAG_TIMER_SERVICE = "TimerService"
    const val TAG_RESCHEDULE_RECEIVER = "RescheduleReceiver"
    const val TAG_RINGTONE_REPOSITORY = "RingtoneRepository"

    const val SCHEME_PACKAGE = "package:"

    const val REQUEST_CODE_TIMER = 100
    const val REQUEST_CODE_STOPWATCH = 200

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

    object Notifications {
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
}
