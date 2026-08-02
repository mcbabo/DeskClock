package app.grapheneos.deskclock.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import app.grapheneos.deskclock.alarm.data.AlarmInstance
import app.grapheneos.deskclock.alarm.util.AlarmConstants

class AlarmController(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleInstance(instance: AlarmInstance, alarmId: Long) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }

        val operation = createReceiverPendingIntent(instance.id, alarmId)
        val showIntent = createShowPendingIntent(alarmId)
        val alarmInfo = AlarmManager.AlarmClockInfo(instance.timeInMillis, showIntent)

        try {
            alarmManager.setAlarmClock(alarmInfo, operation)
        } catch (e: SecurityException) {
            Log.d("AlarmController", e.toString())
        }
    }

    fun cancelInstance(alarmId: Long) {
        val operation = createReceiverPendingIntent(-1L, alarmId)
        alarmManager.cancel(operation)
        operation.cancel()
    }

    private fun createShowPendingIntent(alarmId: Long): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, app.grapheneos.deskclock.MainActivity::class.java)
        
        return PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createReceiverPendingIntent(instanceId: Long, alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            setPackage(context.packageName)
            putExtra(AlarmConstants.EXTRA_INSTANCE_ID, instanceId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
