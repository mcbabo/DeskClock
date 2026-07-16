package app.grapheneos.deskclock.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.grapheneos.deskclock.alarm.data.AlarmDao
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmInstance
import app.grapheneos.deskclock.clock.data.ClockConverters
import app.grapheneos.deskclock.clock.data.ClockDao
import app.grapheneos.deskclock.clock.domain.ClockEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AlarmEntity::class,
        AlarmInstance::class,
        ClockEntity::class
    ],
    version = 2
)
@TypeConverters(ClockConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun clockDao(): ClockDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
