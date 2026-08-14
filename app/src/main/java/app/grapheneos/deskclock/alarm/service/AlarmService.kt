package app.grapheneos.deskclock.alarm.service

import android.content.Intent
import android.content.pm.ServiceInfo
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpActivity
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AlarmService : BaseAlertService(Constants.Alarm.PM_TAG) {
    private val alarmRepository: AlarmRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val notificationManager: AlarmNotificationManager by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instanceId = intent?.getLongExtra(Constants.Alarm.EXTRA_INSTANCE_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(Constants.Alarm.EXTRA_ALARM_LABEL) ?: ""
        val hour = intent?.getIntExtra(Constants.Alarm.EXTRA_ALARM_HOUR, -1) ?: -1
        val minute = intent?.getIntExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, -1) ?: -1

        val hasIntentData = intent?.hasExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI) == true
        val intentRingtone = intent?.getStringExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI)
        val intentVibrate =
            intent?.getBooleanExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, true) ?: true

        val notification = notificationManager.buildAlarmNotification(
            instanceId = instanceId,
            label = label,
            hour = hour,
            minute = minute
        )

        startForeground(
            NotificationConstants.Alarm.NOTIFICATION_ID,
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
                    appSettings.graduallyIncreaseVolumeDuration
                )
            } else {
                startAlert(
                    intentRingtone,
                    intentVibrate,
                    ringtoneVolume,
                    appSettings.graduallyIncreaseVolume,
                    appSettings.graduallyIncreaseVolumeDuration
                )
            }

            if (alarm != null && (label.isEmpty() || hour == -1)) {
                val updatedNotification = notificationManager.buildAlarmNotification(
                    instanceId = instanceId,
                    label = alarm.alarm.label,
                    hour = alarm.alarm.hour,
                    minute = alarm.alarm.minute
                )
                notificationManager.updateNotification(
                    NotificationConstants.Alarm.NOTIFICATION_ID,
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
}
