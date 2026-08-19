package app.grapheneos.deskclock.settings.presentation

import android.net.Uri
import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.data.ThemeMode

/**
 * UI representation of application settings.
 */
@Immutable
data class AppSettingsUiModel(
    val themeMode: ThemeMode,
    val dynamicColors: Boolean,
    val snoozeDurationMinutes: Int,
    val defaultRingtone: RingtoneItem,
    val directBootRingtone: RingtoneItem,
    val useCustomRingtoneVolume: Boolean,
    val ringtoneVolume: Float,
    val vibrate: Boolean,
    val stopwatchShowMilliseconds: Boolean,
    val alarmPopUpStyle: PopUpStyle,
    val timerPopUpStyle: PopUpStyle,
    val graduallyIncreaseVolume: Boolean,
    val graduallyIncreaseVolumeDuration: Int
)

/**
 * Dialogs available on the Settings screen.
 */
enum class SettingsDialog {
    NONE,
    SNOOZE,
    RINGTONE,
    DIRECT_BOOT_RINGTONE,
    VOLUME,
    THEME,
    GRADUALLY_INCREASE_VOLUME
}

/**
 * UI state for the Settings screen.
 */
@Immutable
data class SettingsUiState(
    val settings: AppSettingsUiModel? = null,
    val ringtones: List<RingtoneItem> = emptyList(),
    val internalRingtones: List<RingtoneItem> = emptyList(),
    val isLoading: Boolean = false,
    val visibleDialog: SettingsDialog = SettingsDialog.NONE
)

/**
 * User intents for the Settings screen.
 */
sealed interface SettingsIntent {
    data object LoadSettings : SettingsIntent
    data object ResetDefaults : SettingsIntent
    data object LoadSystemRingtones : SettingsIntent
    data object LoadInternalRingtones : SettingsIntent
    data object RefreshRingtones : SettingsIntent
    data class ImportRingtone(val uri: Uri) : SettingsIntent
    data class DeleteCustomRingtone(val ringtone: RingtoneItem) : SettingsIntent
    data class PlayPreview(val uri: Uri) : SettingsIntent
    data object StopPreview : SettingsIntent

    data class UpdateTheme(val theme: ThemeMode) : SettingsIntent
    data class SetDynamicColors(val enabled: Boolean) : SettingsIntent

    data class SetSnoozeTime(val minutes: Int) : SettingsIntent
    data class SetDefaultRingtone(val uri: Uri) : SettingsIntent
    data class SetDirectBootRingtone(val uri: Uri) : SettingsIntent

    data class SetCustomRingtoneVolumeEnabled(val enabled: Boolean) : SettingsIntent
    data class SetCustomRingtoneVolume(val volume: Float) : SettingsIntent

    data class SetDefaultVibration(val enabled: Boolean) : SettingsIntent

    data class SetAlarmPopUpStyle(val style: PopUpStyle) : SettingsIntent
    data class SetTimerPopUpStyle(val style: PopUpStyle) : SettingsIntent

    data class SetGraduallyIncreaseVolume(val enabled: Boolean) : SettingsIntent
    data class SetGraduallyIncreaseVolumeDuration(val duration: Int) : SettingsIntent
    data class SetVisibleDialog(val dialog: SettingsDialog) : SettingsIntent
}

fun AppSettings.toUiModel(): AppSettingsUiModel {
    return AppSettingsUiModel(
        themeMode = themeMode,
        dynamicColors = dynamicColors,
        snoozeDurationMinutes = snoozeDurationMinutes,
        defaultRingtone = defaultRingtone,
        directBootRingtone = directBootRingtone,
        useCustomRingtoneVolume = useCustomRingtoneVolume,
        ringtoneVolume = ringtoneVolume,
        vibrate = vibrate,
        stopwatchShowMilliseconds = stopwatchShowMilliseconds,
        alarmPopUpStyle = alarmPopUpStyle,
        timerPopUpStyle = timerPopUpStyle,
        graduallyIncreaseVolume = graduallyIncreaseVolume,
        graduallyIncreaseVolumeDuration = graduallyIncreaseVolumeDuration
    )
}
