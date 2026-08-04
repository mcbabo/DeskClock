package app.grapheneos.deskclock.timer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.grapheneos.deskclock.timer.util.TimerConstants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimerReceiver : BroadcastReceiver(), KoinComponent {

    private val timerRepository: TimerRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TimerConstants.ACTION_PAUSE_RESUME -> timerRepository.togglePauseResume()
            TimerConstants.ACTION_RESET -> timerRepository.reset()
        }
    }
}
