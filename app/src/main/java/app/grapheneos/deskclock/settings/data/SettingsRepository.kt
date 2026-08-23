package app.grapheneos.deskclock.settings.data

import android.net.Uri
import app.grapheneos.deskclock.core.database.SettingsDataStore
import kotlinx.coroutines.flow.Flow

/**
 * Data layer for managing all application-wide settings via [SettingsDataStore].
 */
class SettingsRepository(
    private val dataStore: SettingsDataStore
) {
    val settings: Flow<AppSettings> = dataStore.settingsFlow

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.updateSettings(transform)
    }

    suspend fun onRingtoneDeleted(deletedUri: Uri) {
        val defaults = AppSettings()
        update { current ->
            current.copy(
                defaultRingtone = if (current.defaultRingtone.uri == deletedUri) {
                    defaults.defaultRingtone
                } else {
                    current.defaultRingtone
                },
                directBootRingtone = if (current.directBootRingtone.uri == deletedUri) {
                    defaults.directBootRingtone
                } else {
                    current.directBootRingtone
                }
            )
        }
    }
}
