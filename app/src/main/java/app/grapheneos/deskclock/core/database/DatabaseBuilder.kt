package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("deskclock.db")
    return Room.databaseBuilder(
        context = context,
        name = dbFile.absolutePath
    )
}
