package app.grapheneos.deskclock.alarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.core.util.Constants
import org.koin.core.component.KoinComponent

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
