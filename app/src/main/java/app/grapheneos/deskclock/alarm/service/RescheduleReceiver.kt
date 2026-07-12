package app.grapheneos.deskclock.alarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RescheduleReceiver : BroadcastReceiver(), KoinComponent {
    private val repository: AlarmRepository by inject()
    private val controller: AlarmController by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                repository.getAllActiveInstances().forEach { instance ->
                    controller.scheduleInstance(instance, instance.alarmId)
                }
            }
        }
    }
}
