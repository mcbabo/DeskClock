package app.grapheneos.deskclock.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
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

        val pendingIntent = createReceiverPendingIntent(instance.id, alarmId)
        val alarmInfo = AlarmManager.AlarmClockInfo(instance.timeInMillis, pendingIntent)

        try {
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        } catch (e: SecurityException) {
            // TODO()
        }
    }

    fun cancelInstance(alarmId: Long) {
        val pendingIntent = createReceiverPendingIntent(-1L, alarmId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createReceiverPendingIntent(instanceId: Long, alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
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
