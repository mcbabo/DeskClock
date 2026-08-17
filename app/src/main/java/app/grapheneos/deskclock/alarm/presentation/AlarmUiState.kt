package app.grapheneos.deskclock.alarm.presentation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.util.AlarmDayFormatter
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.util.formatSystemTime

/**
 * UI representation of an Alarm.
 */
@Immutable
data class AlarmUiModel(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int,
    val isEnabled: Boolean,
    val deleteAfterUse: Boolean,
    val label: String,
    val ringtoneUri: Uri,
    val vibrate: Boolean,
    val snoozeDurationMinutes: Int,
    val hasInstance: Boolean = false,
    val timeText: String = "",
    val daysOfWeekText: String = ""
)

/**
 * UI state for the Alarm screen.
 */
@Immutable
data class AlarmUiState(
    val alarms: List<AlarmUiModel> = emptyList(),
    val ringtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false,
    val nextAlarmRemainingTime: String? = null
)

/**
 * User intents for the Alarm screen.
 */
sealed interface AlarmIntent {
    // Alarm
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
    data class ImportRingtone(val uri: Uri) : AlarmIntent
    data class DeleteCustomRingtone(val ringtone: RingtoneItem) : AlarmIntent
    data class PlayPreview(val uri: Uri) : AlarmIntent
    data object StopPreview : AlarmIntent
}

fun AlarmWithInstance.toUiModel(context: Context): AlarmUiModel {
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
        hasInstance = instance != null,
        timeText = formatSystemTime(context, alarm.hour, alarm.minute),
        daysOfWeekText = AlarmDayFormatter.formatDaysOfWeek(context, alarm.daysOfWeek)
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
