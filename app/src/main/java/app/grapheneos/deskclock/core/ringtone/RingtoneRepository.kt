package app.grapheneos.deskclock.core.ringtone

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.UriSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

class RingtoneRepository(private val context: Context) {
    private val customRingtonesDir = File(context.filesDir, "custom_ringtones").apply { mkdirs() }

    companion object {
        private val INTERNAL_MAP = mapOf(
            "argon" to "Argon",
            "barium" to "Barium",
            "carbon" to "Carbon",
            "cesium" to "Cesium",
            "helium" to "Helium",
            "krypton" to "Krypton",
            "neon" to "Neon",
            "neptunium" to "Neptunium",
            "osmium" to "Osmium",
            "oxygen" to "Oxygen",
            "platinum" to "Platinum",
            "promethium" to "Promethium",
            "scandium" to "Scandium",
            "piezo_alarm" to "Piezo Alarm",
            "buzzer_alarm" to "Buzzer Alarm",
            "rooster_alarm" to "Rooster Alarm",
            "beepbeep_alarm" to "Beep Beep Alarm",
            "beepbeepbeep_alarm" to "Beep Beep Beep Alarm"
        )
    }

    suspend fun getRingtones(onlyDirectBoot: Boolean = false): List<RingtoneItem> =
        withContext(Dispatchers.IO) {
            if (onlyDirectBoot) {
                return@withContext getInternalRingtones()
            }

            val all = mutableListOf<RingtoneItem>()

            all.addAll(getCustomRingtones())
            all.addAll(getSystemRingtones())

            all
        }

    suspend fun getInternalRingtones(): List<RingtoneItem> = withContext(Dispatchers.IO) {
        INTERNAL_MAP.map { (resName, displayName) ->
            RingtoneItem(
                name = displayName,
                uri = "android.resource://${context.packageName}/raw/$resName".toUri()
            )
        }
    }

    suspend fun getCustomRingtones(): List<RingtoneItem> = withContext(Dispatchers.IO) {
        customRingtonesDir.listFiles()?.map { file ->
            RingtoneItem(
                name = file.name.substringBeforeLast("."),
                uri = file.toUri()
            )
        } ?: emptyList()
    }

    suspend fun getSystemRingtones(): List<RingtoneItem> = withContext(Dispatchers.IO) {
        val ringtones = mutableListOf<RingtoneItem>()
        val manager = RingtoneManager(context).apply { setType(RingtoneManager.TYPE_ALARM) }
        manager.cursor?.let { cursor ->
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position)
                ringtones.add(RingtoneItem(name = title, uri = uri))
            }
        }
        ringtones
    }

    suspend fun getRingtoneItem(uri: Uri): RingtoneItem = withContext(Dispatchers.IO) {
        when (uri.scheme) {
            "android.resource" -> {
                val resName = uri.lastPathSegment ?: ""
                val displayName =
                    INTERNAL_MAP[resName] ?: resName.replaceFirstChar { it.uppercase() }
                RingtoneItem(name = displayName, uri = uri)
            }

            "file" -> {
                RingtoneItem(
                    name = uri.lastPathSegment?.substringBeforeLast(".") ?: "Custom Alarm",
                    uri = uri
                )
            }

            else -> {
                try {
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    RingtoneItem(
                        name = ringtone?.getTitle(context) ?: "Unknown",
                        uri = uri
                    )
                } catch (_: Exception) {
                    RingtoneItem(name = "Unknown", uri = uri)
                }
            }
        }
    }

    /**
     * Imports a file from the system picker into the app's DE storage.
     */
    suspend fun addCustomRingtone(sourceUri: Uri): RingtoneItem? = withContext(Dispatchers.IO) {
        try {
            val extension = getExtension(sourceUri)
            val fileName = getFileName(sourceUri) ?: "custom_${System.currentTimeMillis()}.$extension"
            val destFile = File(customRingtonesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            RingtoneItem(fileName.substringBeforeLast("."), destFile.toUri())
        } catch (e: Exception) {
            Log.e(Constants.TAG_RINGTONE_REPOSITORY, "Failed to import ringtone", e)
            null
        }
    }

    private fun getExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp3"
    }

    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }

    suspend fun deleteCustomRingtone(item: RingtoneItem) = withContext(Dispatchers.IO) {
        val uri = item.uri
        if (uri.scheme == "file") {
            uri.path?.let { File(it).delete() }
        }
    }
}

/**
 * Represents a ringtone available on the system or selected by the user.
 * @param name The user-visible title of the ringtone.
 * @param uri The unique content URI used to play the ringtone.
 */
@Immutable
@Serializable
data class RingtoneItem(
    val name: String,
    @Serializable(with = UriSerializer::class)
    val uri: Uri
)
