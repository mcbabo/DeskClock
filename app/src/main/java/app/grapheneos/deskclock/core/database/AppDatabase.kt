package app.grapheneos.deskclock.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [],
    version = 1
)
abstract class AppDatabase : RoomDatabase()

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
