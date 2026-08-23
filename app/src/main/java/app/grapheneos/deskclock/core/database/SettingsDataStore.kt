package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.AppSettingsSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File

class SettingsDataStore(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val migration = object : DataMigration<AppSettings> {
        override suspend fun shouldMigrate(currentData: AppSettings): Boolean {
            val oldFile = getOldFile()
            val newFile = getNewFile()
            return oldFile.exists() && !newFile.exists()
        }

        override suspend fun migrate(currentData: AppSettings): AppSettings {
            val oldFile = getOldFile()
            return try {
                val oldDataStore = PreferenceDataStoreFactory.create(produceFile = { oldFile })
                val preferences = oldDataStore.data.first()
                val jsonKey = stringPreferencesKey(Constants.SETTINGS_DATASTORE_KEY)
                val settingsJson = preferences[jsonKey]

                if (settingsJson != null) {
                    json.decodeFromString<AppSettings>(settingsJson)
                } else {
                    currentData
                }
            } catch (_: Exception) {
                currentData
            }
        }

        override suspend fun cleanUp() {
            val oldFile = getOldFile()
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }

        private fun getOldFile(): File {
            val deFile = File(
                context.filesDir,
                "datastore/${Constants.SETTINGS_DATASTORE_NAME}.preferences_pb"
            )
            if (deFile.exists()) return deFile

            return File(
                context.filesDir.path.replace("_de", ""),
                "datastore/${Constants.SETTINGS_DATASTORE_NAME}.preferences_pb"
            )
        }
    }

    private fun getNewFile(): File {
        val dir = File(context.filesDir, "datastore")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${Constants.SETTINGS_DATASTORE_NAME}.pb")
    }

    private val dataStore: DataStore<AppSettings> = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { getNewFile() },
        migrations = listOf(migration),
        corruptionHandler = ReplaceFileCorruptionHandler { AppSettings() }
    )

    val settingsFlow: Flow<AppSettings> = dataStore.data

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        dataStore.updateData { current ->
            update(current)
        }
    }
}
