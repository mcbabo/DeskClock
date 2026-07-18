package app.grapheneos.deskclock

import android.app.Application
import app.grapheneos.deskclock.core.di.initializeKoin
import app.grapheneos.deskclock.core.notification.ChannelManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class DeskClockApplication : Application() {
    private val channelManager: ChannelManager by inject()

    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@DeskClockApplication)
        }
        channelManager.createAllChannels()
    }
}
