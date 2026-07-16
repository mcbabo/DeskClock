package app.grapheneos.deskclock.clock.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZoneId

@Entity(tableName = "clocks")
data class ClockEntity(
    @PrimaryKey val zoneId: ZoneId,
    val label: String = ""
)
