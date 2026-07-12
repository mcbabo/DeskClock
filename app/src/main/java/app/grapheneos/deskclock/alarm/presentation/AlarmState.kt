package app.grapheneos.deskclock.alarm.presentation

import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance

data class RingtoneItem(val name: String, val uri: String)

data class AlarmState(
    val alarms: List<AlarmWithInstance> = emptyList(),
    val ringtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface AlarmAction {
    // Alarm
    object LoadAlarms : AlarmAction
    data class ToggleAlarm(val alarm: AlarmEntity) : AlarmAction
    data class UpdateAlarm(val alarm: AlarmEntity) : AlarmAction
    data class DeleteAlarm(val alarm: AlarmEntity) : AlarmAction
    data class AddAlarm(
        val hour: Int,
        val minute: Int,
        val daysOfWeek: Int,
        val deleteAfterUse: Boolean,
        val label: String
    ) : AlarmAction

    // Ringtones
    object LoadSystemRingtones : AlarmAction
    data class PlayPreview(val uri: String) : AlarmAction
    object StopPreview : AlarmAction
}
