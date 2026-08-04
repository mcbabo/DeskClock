package app.grapheneos.deskclock.core.ringtone

import android.content.Context
import android.media.RingtoneManager
import androidx.core.net.toUri
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RingtoneRepository(private val context: Context) {
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

    suspend fun getRingtoneItem(uri: String): RingtoneItem = withContext(Dispatchers.IO) {
        val ringtone = RingtoneManager.getRingtone(context, uri.toUri())
        RingtoneItem(ringtone.getTitle(context), uri)
    }
}
