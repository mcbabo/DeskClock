package app.grapheneos.deskclock.alarm.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.notification.baseNotificationBuilder
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.Intents
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
        if (intent == null) return START_NOT_STICKY
        val alarmData = Intents.Alarm.extractAlarmData(intent) ?: return START_NOT_STICKY

        val instanceId = alarmData.instanceId
        val label = alarmData.label
        val hour = alarmData.hour
        val minute = alarmData.minute

        val hasIntentData = intent.hasExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI)
        val intentRingtone = alarmData.ringtoneUri
        val intentVibrate = alarmData.vibrate

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
        val intent = Intents.Alarm.createAlarmPopUpIntent(this, instanceId, label, hour, minute)
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
        return Intents.Alarm.createAlarmPopUpPendingIntent(this, id, label, hour, minute)
    }
}

/**
 * Helper class to manage the scheduling and cancellation of alarms with the system [AlarmManager].
 */
class AlarmController(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleInstance(alarm: AlarmEntity, instanceId: Long, triggerTime: Long) {
        if (!alarmManager.canScheduleExactAlarms()) {
            context.startActivity(Intents.System.createRequestExactAlarmIntent())
            return
        }

        val operation = Intents.Alarm.createFireAlarmPendingIntent(context, instanceId, alarm)
        val showIntent = Intents.createShowMainActivityPendingIntent(context, alarm.id.toInt())
        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, showIntent)

        try {
            alarmManager.setAlarmClock(alarmInfo, operation)
        } catch (e: SecurityException) {
            Log.e("AlarmController", "Failed to schedule exact alarm", e)
        }
    }

    fun cancelInstance(alarmId: Long) {
        val operation = Intents.Alarm.createCancelAlarmPendingIntent(context, alarmId)
        alarmManager.cancel(operation)
        operation.cancel()
    }
}
