package app.grapheneos.deskclock.timer.service

import android.content.Context
import android.content.Intent

class TimerController(private val context: Context) {
    fun startTimerService() {
        val intent = Intent(context, TimerService::class.java)
        context.startForegroundService(intent)
    }

    fun stopTimerService() {
        val intent = Intent(context, TimerService::class.java)
        context.stopService(intent)
    }
}
