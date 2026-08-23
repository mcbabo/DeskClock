package app.grapheneos.deskclock.settings.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun readFrom(input: InputStream): AppSettings {
        try {
            val content = input.readBytes().decodeToString()
            return if (content.isEmpty()) {
                defaultValue
            } else {
                json.decodeFromString(AppSettings.serializer(), content)
            }
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read AppSettings", e)
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        val content = json.encodeToString(AppSettings.serializer(), t)
        withContext(Dispatchers.IO) {
            output.write(content.encodeToByteArray())
            output.flush()
        }
    }
}
