package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.SETTINGS_DATASTORE_NAME
)

class SettingsDataStore(private val context: Context) {
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
        context.settingsDataStore.data
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
        context.settingsDataStore.edit { preferences ->
            preferences[SETTINGS_JSON_KEY] = json.encodeToString(settings)
        }
    }

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
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
