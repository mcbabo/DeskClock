package app.grapheneos.deskclock.alarm.presentation

import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import kotlinx.serialization.Serializable

@Serializable
data class RingtoneItem(val name: String, val uri: String)

data class AlarmUiState(
    val alarms: List<AlarmWithInstance> = emptyList(),
    val ringtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface AlarmIntent {
    // Alarm
    data object LoadAlarms : AlarmIntent
    data class ToggleAlarm(val alarm: AlarmEntity) : AlarmIntent
    data class UpdateAlarm(val alarm: AlarmEntity) : AlarmIntent
    data class DeleteAlarm(val alarm: AlarmEntity) : AlarmIntent
    data class AddAlarm(
        val hour: Int,
        val minute: Int,
        val daysOfWeek: Int,
        val deleteAfterUse: Boolean,
        val label: String
    ) : AlarmIntent

    // Ringtones
    data object LoadSystemRingtones : AlarmIntent
    data class PlayPreview(val uri: String) : AlarmIntent
    data object StopPreview : AlarmIntent
}
