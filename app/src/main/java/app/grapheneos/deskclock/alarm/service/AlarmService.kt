package app.grapheneos.deskclock.alarm.service

import android.content.Intent
import android.content.pm.ServiceInfo
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.core.audio.VibrationManager
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.core.service.BaseAlertService
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class AlarmService : BaseAlertService(AlarmConstants.PM_TAG) {
    private val repository: AlarmRepository by inject()
    private val notificationManager: AlarmNotificationManager by inject()
    private val vibrationManager: VibrationManager by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instanceId = intent?.getLongExtra(AlarmConstants.EXTRA_INSTANCE_ID, -1L) ?: -1L

        val notification = notificationManager.buildAlarmNotification(instanceId)
        startForeground(
            NotificationConstants.Alarm.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        serviceScope.launch {
            val alarm = repository.getAlarmByInstanceId(instanceId)

            audioPlayer.playAlarm(alarm?.alarm?.ringtoneUri)

            if (alarm?.alarm?.vibrate != false) {
                vibrationManager.startAlarmVibration()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        vibrationManager.stop()
        super.onDestroy()
    }
}
