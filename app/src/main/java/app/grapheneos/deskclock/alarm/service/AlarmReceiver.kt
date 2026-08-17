package app.grapheneos.deskclock.alarm.service

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.Intents
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

        val alarmData = Intents.Alarm.extractAlarmData(intent) ?: return

        val serviceIntent = Intents.Alarm.createAlarmServiceIntent(
            context,
            alarmData.instanceId,
            alarmData.label,
            alarmData.hour,
            alarmData.minute,
            alarmData.ringtoneUri,
            alarmData.vibrate
        )

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
