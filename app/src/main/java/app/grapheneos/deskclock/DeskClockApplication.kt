package app.grapheneos.deskclock

import android.app.Application
import android.os.UserManager
import android.util.Log
import app.grapheneos.deskclock.core.di.initializeKoin
import app.grapheneos.deskclock.core.notification.ChannelManager
import app.grapheneos.deskclock.core.util.Constants
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import java.io.File

class DeskClockApplication : Application() {
    private val channelManager: ChannelManager by inject()

    override fun onCreate() {
        super.onCreate()

        val userManager = getSystemService(UserManager::class.java)
        if (userManager.isUserUnlocked) {
            migrateToDeviceProtectedStorage()
        }

        initializeKoin {
            androidContext(this@DeskClockApplication)
        }
        channelManager.createAllChannels()
    }

    private fun migrateToDeviceProtectedStorage() {
        val deContext = createDeviceProtectedStorageContext()

        val dbName = Constants.DATABASE_NAME
        try {
            if (!deContext.getDatabasePath(dbName).exists()) {
                val success = deContext.moveDatabaseFrom(this, dbName)
                if (success) {
                    Log.d(Constants.TAG_APPLICATION, "Successfully migrated database to DE storage")
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG_APPLICATION, "Failed to migrate database", e)
        }

        val ceDataStoreDir = File(filesDir, "datastore")
        val deDataStoreDir = File(deContext.filesDir, "datastore")

        if (ceDataStoreDir.exists() && ceDataStoreDir.isDirectory) {
            try {
                if (!deDataStoreDir.exists()) {
                    deDataStoreDir.mkdirs()
                }

                ceDataStoreDir.listFiles()?.forEach { file ->
                    val targetFile = File(deDataStoreDir, file.name)
                    if (!targetFile.exists()) {
                        file.copyTo(targetFile, overwrite = true)
                        file.delete()
                    }
                }
                Log.d(
                    Constants.TAG_APPLICATION,
                    "Successfully migrated DataStore directory to DE storage"
                )
            } catch (e: Exception) {
                Log.e(Constants.TAG_APPLICATION, "Failed to migrate DataStore", e)
            }
        }
    }
}
