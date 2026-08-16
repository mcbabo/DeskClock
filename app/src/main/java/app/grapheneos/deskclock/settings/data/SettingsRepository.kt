package app.grapheneos.deskclock.settings.data

import android.net.Uri
import app.grapheneos.deskclock.core.database.SettingsDataStore
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import kotlinx.coroutines.flow.Flow

/**
 * Data layer for managing all application-wide settings via [SettingsDataStore].
 */
class SettingsRepository(
    private val dataStore: SettingsDataStore
) {
    val settings: Flow<AppSettings> = dataStore.settingsFlow

    suspend fun setTheme(themeMode: ThemeMode) {
        dataStore.updateSettings { it.copy(themeMode = themeMode) }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.updateSettings { it.copy(dynamicColors = enabled) }
    }

    suspend fun setSnoozeTime(minutes: Int) {
        dataStore.updateSettings { it.copy(snoozeDurationMinutes = minutes) }
    }

    suspend fun setDefaultRingtone(ringtone: RingtoneItem) {
        dataStore.updateSettings { it.copy(defaultRingtone = ringtone) }
    }

    suspend fun setDirectBootRingtone(ringtone: RingtoneItem) {
        dataStore.updateSettings { it.copy(directBootRingtone = ringtone) }
    }

    suspend fun setCustomRingtoneVolumeEnabled(enabled: Boolean) {
        dataStore.updateSettings {
            it.copy(useCustomRingtoneVolume = enabled)
        }
    }

    suspend fun setRingtoneVolume(volume: Float) {
        dataStore.updateSettings {
            it.copy(ringtoneVolume = volume)
        }
    }

    suspend fun setDefaultVibration(enabled: Boolean) {
        dataStore.updateSettings { it.copy(vibrate = enabled) }
    }

    suspend fun setAlarmPopUpStyle(style: PopUpStyle) {
        dataStore.updateSettings { it.copy(alarmPopUpStyle = style) }
    }

    suspend fun setTimerPopUpStyle(style: PopUpStyle) {
        dataStore.updateSettings { it.copy(timerPopUpStyle = style) }
    }

    suspend fun setGraduallyIncreaseVolume(enabled: Boolean) {
        dataStore.updateSettings { it.copy(graduallyIncreaseVolume = enabled) }
    }

    suspend fun setGraduallyIncreaseVolumeDuration(duration: Int) {
        dataStore.updateSettings { it.copy(graduallyIncreaseVolumeDuration = duration) }
    }

    suspend fun resetToDefaults() {
        dataStore.updateSettings { AppSettings() }
    }

    suspend fun onRingtoneDeleted(deletedUri: Uri) {
        val defaults = AppSettings()
        dataStore.updateSettings { current ->
            var updated = current
            if (current.defaultRingtone.uri == deletedUri) {
                updated = updated.copy(defaultRingtone = defaults.defaultRingtone)
            }
            if (current.directBootRingtone.uri == deletedUri) {
                updated = updated.copy(directBootRingtone = defaults.directBootRingtone)
            }
            updated
        }
    }
}
