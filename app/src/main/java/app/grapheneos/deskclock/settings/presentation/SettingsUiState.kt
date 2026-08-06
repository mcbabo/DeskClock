package app.grapheneos.deskclock.settings.presentation

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.ThemeMode

@Immutable
data class AppSettingsUiModel(
    val themeMode: ThemeMode,
    val dynamicColors: Boolean,
    val snoozeDurationMinutes: Int,
    val defaultRingtone: RingtoneItem,
    val useCustomRingtoneVolume: Boolean,
    val ringtoneVolume: Float,
    val vibrate: Boolean,
    val stopwatchShowMilliseconds: Boolean
)

@Immutable
data class SettingsUiState(
    val settings: AppSettingsUiModel? = null,
    val ringtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface SettingsIntent {
    data object LoadSettings : SettingsIntent
    data object ResetDefaults : SettingsIntent
    data object LoadSystemRingtones : SettingsIntent
    data class PlayPreview(val uri: String) : SettingsIntent
    data object StopPreview : SettingsIntent

    data class UpdateTheme(val theme: ThemeMode) : SettingsIntent
    data class SetDynamicColors(val enabled: Boolean) : SettingsIntent

    data class SetSnoozeTime(val minutes: Int) : SettingsIntent
    data class SetDefaultRingtone(val uri: String) : SettingsIntent

    data class SetCustomRingtoneVolumeEnabled(val enabled: Boolean) : SettingsIntent
    data class SetCustomRingtoneVolume(val volume: Float) : SettingsIntent

    data class SetDefaultVibration(val enabled: Boolean) : SettingsIntent
}

fun AppSettings.toUiModel(): AppSettingsUiModel {
    return AppSettingsUiModel(
        themeMode = themeMode,
        dynamicColors = dynamicColors,
        snoozeDurationMinutes = snoozeDurationMinutes,
        defaultRingtone = defaultRingtone,
        useCustomRingtoneVolume = useCustomRingtoneVolume,
        ringtoneVolume = ringtoneVolume,
        vibrate = vibrate,
        stopwatchShowMilliseconds = stopwatchShowMilliseconds
    )
}
