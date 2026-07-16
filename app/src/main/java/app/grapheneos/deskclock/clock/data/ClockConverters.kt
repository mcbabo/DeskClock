package app.grapheneos.deskclock.clock.data

import androidx.room.TypeConverter
import java.time.ZoneId

class ClockConverters {
    @TypeConverter
    fun fromZoneId(zoneId: ZoneId): String = zoneId.id

    @TypeConverter
    fun toZoneId(value: String): ZoneId = ZoneId.of(value)
}
