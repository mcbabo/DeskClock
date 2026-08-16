package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.File

class SettingsDataStore(context: Context) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = {
            File(context.filesDir, "datastore/${Constants.SETTINGS_DATASTORE_NAME}.preferences_pb")
        }
    )

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    companion object {
        private val SETTINGS_JSON_KEY = stringPreferencesKey(Constants.SETTINGS_DATASTORE_KEY)
    }

    suspend fun exportSettingsToJson(): String {
        val currentSettings = settingsFlow.first()
        val jsonString = json.encodeToString(currentSettings)
        return jsonString
    }

    val settingsFlow: Flow<AppSettings> =
        dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }.map { preferences ->
                val settingsJson = preferences[SETTINGS_JSON_KEY]
                if (settingsJson != null) {
                    try {
                        json.decodeFromString<AppSettings>(settingsJson)
                    } catch (_: Exception) {
                        AppSettings()
                    }
                } else {
                    AppSettings()
                }
            }

    suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[SETTINGS_JSON_KEY] = json.encodeToString(settings)
        }
    }

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            val currentSettingsJson = preferences[SETTINGS_JSON_KEY]
            val currentSettings =
                if (currentSettingsJson != null) {
                    try {
                        json.decodeFromString<AppSettings>(currentSettingsJson)
                    } catch (_: Exception) {
                        AppSettings()
                    }
                } else {
                    AppSettings()
                }

            val updatedSettings = update(currentSettings)
            preferences[SETTINGS_JSON_KEY] = json.encodeToString(updatedSettings)
        }
    }
}
