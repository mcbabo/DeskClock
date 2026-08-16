package app.grapheneos.deskclock.alarm.service

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Responds to alarm triggers from the system [AlarmManager] and starts the [AlarmService].
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Constants.Alarm.ACTION_FIRE_ALARM) return

        val instanceId = intent.getLongExtra(Constants.Alarm.EXTRA_INSTANCE_ID, -1L)
        val label = intent.getStringExtra(Constants.Alarm.EXTRA_ALARM_LABEL)
        val hour = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_HOUR, -1)
        val minute = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, -1)
        val ringtoneUri = intent.getStringExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI)
        val vibrate = intent.getBooleanExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, true)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            setPackage(context.packageName)
            putExtra(Constants.Alarm.EXTRA_INSTANCE_ID, instanceId)
            putExtra(Constants.Alarm.EXTRA_ALARM_LABEL, label)
            putExtra(Constants.Alarm.EXTRA_ALARM_HOUR, hour)
            putExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, minute)
            putExtra(Constants.Alarm.EXTRA_ALARM_RINGTONE_URI, ringtoneUri)
            putExtra(Constants.Alarm.EXTRA_ALARM_VIBRATE, vibrate)
        }

        context.startForegroundService(serviceIntent)
    }
}

/**
 * Responds to system boot or app updates to reschedule all active alarms.
 */
class RescheduleReceiver : BroadcastReceiver(), KoinComponent {
    private val alarmRepository: AlarmRepository by inject()
    private val controller: AlarmController by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()

            scope.launch {
                try {
                    val instances = alarmRepository.getAllActiveInstances()

                    instances.forEach { instance ->
                        val alarmWithInstance = alarmRepository.getAlarmByInstanceId(instance.id)
                        if (alarmWithInstance != null) {
                            controller.scheduleInstance(
                                alarm = alarmWithInstance.alarm,
                                instanceId = instance.id,
                                triggerTime = instance.timeInMillis
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(Constants.TAG_RESCHEDULE_RECEIVER, "Failed to reschedule alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
