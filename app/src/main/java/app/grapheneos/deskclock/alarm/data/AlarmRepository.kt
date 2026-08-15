package app.grapheneos.deskclock.alarm.data

import app.grapheneos.deskclock.alarm.service.AlarmController
import app.grapheneos.deskclock.alarm.util.AlarmTimeCalculator
import app.grapheneos.deskclock.core.util.Constants
import kotlinx.coroutines.flow.Flow

/**
 * Data layer for managing Alarms and their active instances.
 * Orchestrates between [AlarmDao] for persistence and [AlarmController] for system scheduling.
 */
class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val alarmController: AlarmController
) {
    val allAlarms: Flow<List<AlarmWithInstance>> = alarmDao.getAllAlarmsWithInstances()

    suspend fun getAllActiveInstances(): List<AlarmInstance> {
        return alarmDao.getAllInstances()
    }

    suspend fun addAlarm(alarm: AlarmEntity) {
        val alarmId = alarmDao.insertAlarm(alarm)
        scheduleInstance(alarm.copy(id = alarmId))
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        alarmDao.updateAlarm(updatedAlarm)
        if (updatedAlarm.isEnabled) {
            scheduleInstance(updatedAlarm)
        } else {
            cancelInstanceByAlarmId(updatedAlarm.id)
        }
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
        if (alarm.isEnabled) {
            scheduleInstance(alarm)
        } else {
            cancelInstanceByAlarmId(alarm.id)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        cancelInstanceByAlarmId(alarm.id)
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun dismissAlarm(instanceId: Long) {
        val alarmWithInstance = alarmDao.getAlarmWithInstanceByInstanceId(instanceId) ?: return
        val alarm = alarmWithInstance.alarm

        cancelInstanceByAlarmId(alarm.id)

        if (alarm.deleteAfterUse) {
            alarmDao.deleteAlarm(alarm)
        } else if (alarm.daysOfWeek != 0) {
            scheduleInstance(alarm)
        } else {
            alarmDao.updateAlarm(alarm.copy(isEnabled = false))
        }
    }

    suspend fun snoozeAlarm(
        instanceId: Long,
        snoozeMinutes: Int = Constants.Alarm.DEFAULT_SNOOZE_TIME
    ) {
        val alarmWithInstance = alarmDao.getAlarmWithInstanceByInstanceId(instanceId) ?: return
        val alarm = alarmWithInstance.alarm

        alarmController.cancelInstance(alarm.id)
        alarmDao.deleteInstancesForAlarm(alarm.id)

        val snoozeTimeInMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
        val snoozeInstance = AlarmInstance(
            alarmId = alarm.id,
            timeInMillis = snoozeTimeInMillis,
            alarmState = 2
        )

        val newInstanceId = alarmDao.insertInstance(snoozeInstance)
        alarmController.scheduleInstance(alarm, newInstanceId, snoozeTimeInMillis)
    }

    private suspend fun scheduleInstance(alarm: AlarmEntity) {
        cancelInstanceByAlarmId(alarm.id)

        val triggerTime = AlarmTimeCalculator.calculateNextTriggerTime(
            alarm.hour,
            alarm.minute,
            alarm.daysOfWeek
        )
        val instance = AlarmInstance(alarmId = alarm.id, timeInMillis = triggerTime, alarmState = 0)
        val instanceId = alarmDao.insertInstance(instance)

        alarmController.scheduleInstance(alarm, instanceId, triggerTime)
    }

    private suspend fun cancelInstanceByAlarmId(alarmId: Long) {
        alarmController.cancelInstance(alarmId)
        alarmDao.deleteInstancesForAlarm(alarmId)
    }

    suspend fun getAlarmByInstanceId(instanceId: Long): AlarmWithInstance? {
        return alarmDao.getAlarmWithInstanceByInstanceId(instanceId)
    }
}
