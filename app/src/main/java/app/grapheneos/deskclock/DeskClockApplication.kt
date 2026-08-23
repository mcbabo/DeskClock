package app.grapheneos.deskclock

import android.app.Application
import android.os.UserManager
import android.util.Log
import app.grapheneos.deskclock.core.di.initializeKoin
import app.grapheneos.deskclock.core.notification.ChannelManager
import app.grapheneos.deskclock.core.util.Constants
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class DeskClockApplication : Application() {
    private val channelManager: ChannelManager by inject()

    override fun onCreate() {
        super.onCreate()

        val userManager = getSystemService(UserManager::class.java)
        if (userManager.isUserUnlocked) {
            migrateDatabaseToDeviceProtectedStorage()
        }

        initializeKoin {
            androidContext(this@DeskClockApplication)
        }
        channelManager.createAllChannels()
    }

    private fun migrateDatabaseToDeviceProtectedStorage() {
        val deContext = createDeviceProtectedStorageContext()
        val dbName = Constants.DATABASE_NAME
        try {
            if (!deContext.getDatabasePath(dbName).exists()) {
                deContext.moveDatabaseFrom(this, dbName)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG_APPLICATION, "Failed to migrate database", e)
        }
    }
}
