package app.grapheneos.deskclock.stopwatch.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.ActivityCompat
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import app.grapheneos.deskclock.stopwatch.util.StopwatchConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class StopwatchService : BaseAlertService(StopwatchConstants.PM_TAG) {
    private val repository: StopwatchRepository by inject()
    private val notificationManager: StopwatchNotificationManager by inject()
    private var observationJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeStopwatch()
        return START_STICKY
    }

    private fun observeStopwatch() {
        if (observationJob != null) return
        observationJob = serviceScope.launch {
            combine(repository.state, repository.elapsedMillis) { state, elapsed ->
                state to (elapsed / 1000)
            }
                .distinctUntilChanged()
                .collect { (state, _) ->
                    val isActive = state.isRunning || state.accumulatedMillis > 0
                    if (isActive) {
                        showNotification()
                    } else {
                        stopSelf()
                    }
                }
        }
    }

    private fun showNotification() {
        val state = repository.state.value
        val notification = notificationManager.buildNotification(state)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(
                NotificationConstants.Stopwatch.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            Log.e("StopwatchService", "Notification permission NOT granted")
        }
    }
}
