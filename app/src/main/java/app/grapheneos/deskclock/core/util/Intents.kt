package app.grapheneos.deskclock.core.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import app.grapheneos.deskclock.MainActivity
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpActivity
import app.grapheneos.deskclock.alarm.service.AlarmReceiver
import app.grapheneos.deskclock.alarm.service.AlarmService
import app.grapheneos.deskclock.stopwatch.data.StopwatchReceiver
import app.grapheneos.deskclock.stopwatch.service.StopwatchService
import app.grapheneos.deskclock.timer.data.TimerReceiver
import app.grapheneos.deskclock.timer.presentation.popup.TimerPopUpActivity
import app.grapheneos.deskclock.timer.service.TimerService

/**
 * Unified Intent and PendingIntent utilities for the application.
 */
object Intents {

    /**
     * Creates a PendingIntent to show the [MainActivity].
     */
    fun createShowMainActivityPendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    object Alarm {
        /**
         * Data class representing extracted alarm information from an Intent.
         */
        data class AlarmData(
            val instanceId: Long,
            val label: String,
            val hour: Int,
            val minute: Int,
            val ringtoneUri: Uri? = null,
            val vibrate: Boolean = true
        )

        /**
         * Extracts alarm data from an [Intent].
         */
        fun extractAlarmData(intent: Intent): AlarmData? {
            val instanceId = intent.getLongExtra(Constants.Alarm.EXTRA_INSTANCE_ID, -1L)
            val hour = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_HOUR, -1)
            val minute = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, -1)

            if (instanceId == -1L || hour == -1 || minute == -1) return null

            return AlarmData(
                instanceId = instanceId,
                label = intent.getStringExtra(Constants.Alarm.EXTRA_ALARM_LABEL) ?: "",
                hour = hour,
                minute = minute,
                ringtoneUri = intent.getParcelableExtra(
                    Constants.Alarm.EXTRA_ALARM_RINGTONE_URI,
                    Uri::class.java
                ),
                vibrate = intent.getBooleanExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, true)
            )
        }

        private fun createAlarmReceiverIntent(context: Context): Intent {
            return Intent(context, AlarmReceiver::class.java).apply {
                action = Constants.Alarm.ACTION_FIRE_ALARM
            }
        }

        /**
         * Creates an Intent to trigger an alarm in [AlarmReceiver].
         */
        fun createFireAlarmIntent(
            context: Context,
            instanceId: Long,
            alarm: AlarmEntity
        ): Intent {
            return createAlarmReceiverIntent(context).apply {
                putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
                putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, alarm.label)
                putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, alarm.hour)
                putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, alarm.minute)
                putExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI, alarm.ringtoneUri)
                putExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, alarm.vibrate)
            }
        }

        /**
         * Creates a PendingIntent to trigger an alarm in [AlarmReceiver].
         */
        fun createFireAlarmPendingIntent(
            context: Context,
            instanceId: Long,
            alarm: AlarmEntity
        ): PendingIntent {
            val intent = createFireAlarmIntent(context, instanceId, alarm)
            return PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Creates a PendingIntent to cancel an alarm in [AlarmReceiver].
         */
        fun createCancelAlarmPendingIntent(context: Context, alarmId: Long): PendingIntent {
            val intent = createAlarmReceiverIntent(context)
            return PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Creates an Intent to start [AlarmService].
         */
        fun createAlarmServiceIntent(
            context: Context,
            instanceId: Long,
            label: String?,
            hour: Int,
            minute: Int,
            ringtoneUri: Uri?,
            vibrate: Boolean
        ): Intent {
            return Intent(context, AlarmService::class.java).apply {
                setPackage(context.packageName)
                putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
                putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, label)
                putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, hour)
                putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, minute)
                putExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI, ringtoneUri)
                putExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, vibrate)
            }
        }

        /**
         * Creates an Intent to launch [AlarmPopUpActivity].
         */
        fun createAlarmPopUpIntent(
            context: Context,
            instanceId: Long,
            label: String,
            hour: Int,
            minute: Int
        ): Intent {
            return Intent(context, AlarmPopUpActivity::class.java).apply {
                putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
                putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, label)
                putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, hour)
                putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, minute)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }

        /**
         * Creates a PendingIntent to launch [AlarmPopUpActivity].
         */
        fun createAlarmPopUpPendingIntent(
            context: Context,
            instanceId: Long,
            label: String,
            hour: Int,
            minute: Int
        ): PendingIntent {
            val intent = createAlarmPopUpIntent(context, instanceId, label, hour, minute)
            return PendingIntent.getActivity(
                context,
                instanceId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    object Timer {
        /**
         * Creates an Intent to start [TimerService].
         */
        fun createTimerServiceIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java)
        }

        /**
         * Creates an Intent to launch [TimerPopUpActivity].
         */
        fun createTimerPopUpIntent(context: Context): Intent {
            return Intent(context, TimerPopUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        /**
         * Creates a PendingIntent for [TimerReceiver] with a specific action.
         */
        fun createTimerReceiverPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, TimerReceiver::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_TIMER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    object Stopwatch {
        /**
         * Creates an Intent to start [StopwatchService].
         */
        fun createStopwatchServiceIntent(context: Context): Intent {
            return Intent(context, StopwatchService::class.java)
        }

        /**
         * Creates a PendingIntent for [StopwatchReceiver] with a specific action.
         */
        fun createStopwatchReceiverPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, StopwatchReceiver::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_STOPWATCH,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    object System {
        /**
         * Creates an Intent to manage overlay permission for the app.
         */
        fun createManageOverlayPermissionIntent(context: Context): Intent {
            return Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "${Constants.SCHEME_PACKAGE}${context.packageName}".toUri()
            )
        }

        /**
         * Creates an Intent to request ignoring battery optimizations for the app.
         */
        @SuppressLint("BatteryLife")
        fun createRequestIgnoreBatteryOptimizationsIntent(context: Context): Intent {
            return Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "${Constants.SCHEME_PACKAGE}${context.packageName}".toUri()
            )
        }

        /**
         * Creates an Intent to request exact alarm permission for the app.
         */
        fun createRequestExactAlarmIntent(): Intent {
            return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
