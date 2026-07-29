package app.grapheneos.deskclock.timer.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.ActivityCompat
import app.grapheneos.deskclock.core.notification.NotificationConstants
import app.grapheneos.deskclock.core.service.BaseAlertService
import app.grapheneos.deskclock.timer.data.TimerData
import app.grapheneos.deskclock.timer.data.TimerRepository
import app.grapheneos.deskclock.timer.presentation.popup.TimerPopUpActivity
import app.grapheneos.deskclock.timer.util.TimerConstants
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class TimerService : BaseAlertService(TimerConstants.PM_TAG) {
    private val repository: TimerRepository by inject()
    private val notificationManager: TimerNotificationManager by inject()

    private var isSoundPlaying = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeTimer()
        return START_STICKY
    }

    private fun observeTimer() {
        serviceScope.launch {
            combine(repository.state, repository.remainingMillis) { state, remaining ->
                state to (remaining / 1000)
            }
                .distinctUntilChanged()
                .collect { (state, _) ->
                    val remaining = state.getRemainingTime()

                    if (state.isStarted && remaining > 0) {
                        showNotification(state)
                    } else if (state.isFinished) {
                        if (!isSoundPlaying) {
                            isSoundPlaying = true
                            launchPopUp()
                            startAlert()
                        }
                        showNotification(state)
                    } else {
                        stopSelf()
                    }
                }
        }
    }

    private fun launchPopUp() {
        val intent = Intent(this, TimerPopUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun showNotification(state: TimerData) {
        val notification = notificationManager.buildTimerNotification(state)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(
                NotificationConstants.Timer.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            Log.e("TimerService", "Notification permission NOT granted")
        }
    }
}
