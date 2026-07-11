package app.grapheneos.deskclock.alarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import org.koin.core.component.KoinComponent

class AlarmReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmConstants.ACTION_FIRE_ALARM) return
        
        val instanceId = intent.getLongExtra(AlarmConstants.EXTRA_INSTANCE_ID, -1L)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_INSTANCE_ID, instanceId)
        }

        context.startForegroundService(serviceIntent)
    }
}
