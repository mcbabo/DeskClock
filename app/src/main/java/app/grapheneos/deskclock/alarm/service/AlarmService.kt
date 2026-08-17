package app.grapheneos.deskclock.alarm.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.MainActivity
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpActivity
import app.grapheneos.deskclock.core.notification.baseNotificationBuilder
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service that handles the ringing state of an active alarm.
 * Manages notification display, audio playback, and vibration.
 */
class AlarmService : BaseAlertService(Constants.Alarm.PM_TAG) {
    private val alarmRepository: AlarmRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instanceId = intent?.getLongExtra(Constants.Alarm.EXTRA_INSTANCE_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(Constants.Alarm.EXTRA_ALARM_LABEL) ?: ""
        val hour = intent?.getIntExtra(Constants.Alarm.EXTRA_ALARM_HOUR, -1) ?: -1
        val minute = intent?.getIntExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, -1) ?: -1

        val hasIntentData = intent?.hasExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI) == true
        val intentRingtone = intent?.getStringExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI)
        val intentVibrate =
            intent?.getBooleanExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, true) ?: true

        val notification = buildAlarmNotification(
            instanceId = instanceId,
            label = label,
            hour = hour,
            minute = minute
        )

        startForeground(
            Constants.Notifications.Alarm.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        launchPopUp(instanceId, label, hour, minute)

        serviceScope.launch {
            val appSettings = settingsRepository.settings.first()
            val ringtoneVolume = if (appSettings.useCustomRingtoneVolume) {
                appSettings.ringtoneVolume
            } else {
                null
            }

            val alarm = alarmRepository.getAlarmByInstanceId(instanceId)
            val dbRingtone = alarm?.alarm?.ringtoneUri
            val dbVibrate = alarm?.alarm?.vibrate ?: true

            if (!hasIntentData || intentRingtone != dbRingtone || intentVibrate != dbVibrate) {
                startAlert(
                    dbRingtone,
                    dbVibrate,
                    ringtoneVolume,
                    appSettings.graduallyIncreaseVolume,
                    appSettings.graduallyIncreaseVolumeDuration,
                    appSettings.directBootRingtone.uri
                )
            } else {
                startAlert(
                    intentRingtone,
                    intentVibrate,
                    ringtoneVolume,
                    appSettings.graduallyIncreaseVolume,
                    appSettings.graduallyIncreaseVolumeDuration,
                    appSettings.directBootRingtone.uri
                )
            }

            if (alarm != null && (label.isEmpty() || hour == -1)) {
                val updatedNotification = buildAlarmNotification(
                    instanceId = instanceId,
                    label = alarm.alarm.label,
                    hour = alarm.alarm.hour,
                    minute = alarm.alarm.minute
                )
                updateNotification(
                    Constants.Notifications.Alarm.NOTIFICATION_ID,
                    updatedNotification
                )
            }
        }

        return START_STICKY
    }

    private fun launchPopUp(instanceId: Long, label: String, hour: Int, minute: Int) {
        val intent = Intent(this, AlarmPopUpActivity::class.java).apply {
            putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
            putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, label)
            putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, hour)
            putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun buildAlarmNotification(
        instanceId: Long,
        label: String,
        hour: Int,
        minute: Int
    ): Notification {
        val fullScreenIntent = getFullScreenIntent(instanceId, label, hour, minute)
        return baseNotificationBuilder(Constants.Notifications.Alarm.CHANNEL_ID)
            .setContentTitle(label.ifEmpty { getString(R.string.alarm) })
            .setContentText(getString(R.string.ringing_dots))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullScreenIntent, true)
            .setContentIntent(fullScreenIntent)
            .setOngoing(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setLocalOnly(true)
            .build()
    }

    private fun updateNotification(notificationId: Int, notification: Notification) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    private fun getFullScreenIntent(
        id: Long,
        label: String,
        hour: Int,
        minute: Int
    ): PendingIntent {
        val intent = Intent().apply {
            component = ComponentName(this@AlarmService, AlarmPopUpActivity::class.java)
            putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, id)
            putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, label)
            putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, hour)
            putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * Helper class to manage the scheduling and cancellation of alarms with the system [AlarmManager].
 */
class AlarmController(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleInstance(alarm: AlarmEntity, instanceId: Long, triggerTime: Long) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        val operation = createReceiverPendingIntent(instanceId, alarm)
        val showIntent = createShowPendingIntent(alarm.id)
        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, showIntent)

        try {
            alarmManager.setAlarmClock(alarmInfo, operation)
        } catch (e: SecurityException) {
            Log.e("AlarmController", "Failed to schedule exact alarm", e)
        }
    }

    fun cancelInstance(alarmId: Long) {
        val intent = Intent().apply {
            component = ComponentName(context, AlarmReceiver::class.java)
            action = Constants.Alarm.ACTION_FIRE_ALARM
        }
        val operation = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(operation)
        operation.cancel()
    }

    private fun createShowPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent().apply {
            component = ComponentName(context, MainActivity::class.java)
        }

        return PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createReceiverPendingIntent(instanceId: Long, alarm: AlarmEntity): PendingIntent {
        val intent = Intent().apply {
            component = ComponentName(context, AlarmReceiver::class.java)
            action = Constants.Alarm.ACTION_FIRE_ALARM
            putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
            putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
