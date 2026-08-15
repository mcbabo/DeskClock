package app.grapheneos.deskclock.stopwatch.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.core.util.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles broadcast intents from Stopwatch notifications (e.g., Lap, Reset, Start/Pause).
 */
class StopwatchReceiver : BroadcastReceiver(), KoinComponent {
    private val stopwatchRepository: StopwatchRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.Stopwatch.ACTION_START_PAUSE -> {
                if (stopwatchRepository.state.value.isRunning) {
                    stopwatchRepository.pause()
                } else {
                    stopwatchRepository.start()
                }
            }

            Constants.Stopwatch.ACTION_LAP_RESET -> {
                if (stopwatchRepository.state.value.isRunning) {
                    stopwatchRepository.lap()
                } else {
                    stopwatchRepository.reset()
                }
            }
        }
    }
}
