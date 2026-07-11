package app.grapheneos.deskclock.alarm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instances",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("alarmId")]
)
data class AlarmInstance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val timeInMillis: Long,
    val alarmState: Int = 0
)
