package app.grapheneos.deskclock.alarm.presentation

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RingtoneItem(val name: String, val uri: String)

@Immutable
data class AlarmUiModel(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int,
    val isEnabled: Boolean,
    val deleteAfterUse: Boolean,
    val label: String,
    val ringtoneUri: String,
    val vibrate: Boolean,
    val snoozeDurationMinutes: Int,
    val hasInstance: Boolean = false
)

@Immutable
data class AlarmUiState(
    val alarms: List<AlarmUiModel> = emptyList(),
    val ringtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface AlarmIntent {
    // Alarm
    data object LoadAlarms : AlarmIntent
    data class ToggleAlarm(val alarm: AlarmUiModel) : AlarmIntent
    data class UpdateAlarm(val alarm: AlarmUiModel) : AlarmIntent
    data class DeleteAlarm(val alarm: AlarmUiModel) : AlarmIntent
    data class RestoreAlarm(val alarm: AlarmUiModel) : AlarmIntent
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

fun AlarmWithInstance.toUiModel(): AlarmUiModel {
    return AlarmUiModel(
        id = alarm.id,
        hour = alarm.hour,
        minute = alarm.minute,
        daysOfWeek = alarm.daysOfWeek,
        isEnabled = alarm.isEnabled,
        deleteAfterUse = alarm.deleteAfterUse,
        label = alarm.label,
        ringtoneUri = alarm.ringtoneUri,
        vibrate = alarm.vibrate,
        snoozeDurationMinutes = alarm.snoozeDurationMinutes,
        hasInstance = instance != null
    )
}

fun AlarmUiModel.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        hour = hour,
        minute = minute,
        daysOfWeek = daysOfWeek,
        isEnabled = isEnabled,
        deleteAfterUse = deleteAfterUse,
        label = label,
        ringtoneUri = ringtoneUri,
        vibrate = vibrate,
        snoozeDurationMinutes = snoozeDurationMinutes
    )
}
