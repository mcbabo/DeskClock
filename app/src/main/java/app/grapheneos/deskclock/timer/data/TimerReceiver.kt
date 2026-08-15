package app.grapheneos.deskclock.timer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.core.util.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles broadcast intents from Timer notifications (e.g., Pause, Reset).
 */
class TimerReceiver : BroadcastReceiver(), KoinComponent {

    private val timerRepository: TimerRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.Timer.ACTION_PAUSE_RESUME -> timerRepository.togglePauseResume()
            Constants.Timer.ACTION_RESET -> timerRepository.reset()
        }
    }
}
