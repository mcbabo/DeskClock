package app.grapheneos.deskclock.core.ringtone

import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class RingtoneRepository(private val context: Context) {
    companion object {
        private val RAW_DISPLAY_NAMES = mapOf(
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

    suspend fun getSystemAlarms(): List<RingtoneItem> = withContext(Dispatchers.IO) {
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_ALARM)
        }
        val cursor = manager.cursor ?: return@withContext emptyList()
        val list = mutableListOf<RingtoneItem>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position).toString()
            list.add(RingtoneItem(title, uri))
        }
        list
    }

    suspend fun getRawRingtones(): List<RingtoneItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RingtoneItem>()
        val fields = R.raw::class.java.fields
        for (field in fields) {
            try {
                val name = field.name
                val displayName =
                    RAW_DISPLAY_NAMES[name] ?: name.replaceFirstChar { it.uppercase() }
                val uri = "android.resource://${context.packageName}/raw/$name"
                list.add(RingtoneItem(displayName, uri))
            } catch (e: Exception) {
                Log.e(Constants.TAG_RINGTONE_REPOSITORY, e.toString())
            }
        }
        list
    }

    suspend fun getRingtoneItem(uri: String): RingtoneItem = withContext(Dispatchers.IO) {
        val ringtone = RingtoneManager.getRingtone(context, uri.toUri())
        RingtoneItem(ringtone.getTitle(context), uri)
    }
}

/**
 * Represents a ringtone available on the system or selected by the user.
 * @param name The user-visible title of the ringtone.
 * @param uri The unique content URI used to play the ringtone.
 */
@Immutable
@Serializable
data class RingtoneItem(val name: String, val uri: String)
