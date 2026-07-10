package app.grapheneos.deskclock

import android.app.Application
import app.grapheneos.deskclock.core.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class DeskClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@DeskClockApplication)
        }
    }
}
