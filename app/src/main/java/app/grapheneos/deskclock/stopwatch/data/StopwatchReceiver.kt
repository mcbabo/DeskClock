package app.grapheneos.deskclock.stopwatch.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.stopwatch.util.StopwatchConstants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StopwatchReceiver : BroadcastReceiver(), KoinComponent {
    private val repository: StopwatchRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            StopwatchConstants.ACTION_START_PAUSE -> {
                if (repository.state.value.isRunning) {
                    repository.pause()
                } else {
                    repository.start()
                }
            }

            StopwatchConstants.ACTION_LAP_RESET -> {
                if (repository.state.value.isRunning) {
                    repository.lap()
                } else {
                    repository.reset()
                }
            }
        }
    }
}
