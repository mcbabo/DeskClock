package app.grapheneos.deskclock.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.grapheneos.deskclock.core.util.Constants

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath(Constants.DATABASE_NAME)
    return Room.databaseBuilder(
        context = context,
        name = dbFile.absolutePath
    )
}
