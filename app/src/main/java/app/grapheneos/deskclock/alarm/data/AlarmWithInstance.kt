package app.grapheneos.deskclock.alarm.data

import androidx.room.Embedded
import androidx.room.Relation

data class AlarmWithInstance(
    @Embedded
    val alarm: AlarmEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "alarmId"
    )
    val instance: AlarmInstance?
)
