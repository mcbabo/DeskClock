package app.grapheneos.deskclock.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.grapheneos.deskclock.alarm.data.AlarmDao
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmInstance
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AlarmEntity::class,
        AlarmInstance::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
