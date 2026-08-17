package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.grapheneos.deskclock.alarm.data.AlarmDao
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmInstance
import app.grapheneos.deskclock.clock.data.ClockConverters
import app.grapheneos.deskclock.clock.data.ClockDao
import app.grapheneos.deskclock.clock.data.ClockEntity
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.UriConverter
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AlarmEntity::class,
        AlarmInstance::class,
        ClockEntity::class
    ],
    version = 2
)
@TypeConverters(ClockConverters::class, UriConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun clockDao(): ClockDao
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    )
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
