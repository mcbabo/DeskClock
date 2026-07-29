package app.grapheneos.deskclock.stopwatch.service

import android.content.Context
import android.content.Intent

class StopwatchController(private val context: Context) {
    fun startService() {
        val intent = Intent(context, StopwatchService::class.java)
        context.startForegroundService(intent)
    }

    fun stopService() {
        val intent = Intent(context, StopwatchService::class.java)
        context.stopService(intent)
    }
}
