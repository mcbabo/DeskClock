package app.grapheneos.deskclock.alarm.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int,
    val isEnabled: Boolean,
    val deleteAfterUse: Boolean = false,
    val label: String = "",
    val ringtoneUri: Uri = Uri.EMPTY,
    val vibrate: Boolean = true,
    val snoozeDurationMinutes: Int = 10
)
