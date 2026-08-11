package app.grapheneos.deskclock.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import app.grapheneos.deskclock.MainActivity
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.core.util.Constants

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
