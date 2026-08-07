package app.grapheneos.deskclock.alarm.presentation.popup

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.settings.data.PopUpStyle

@Immutable
data class AlarmPopUpUiState(
    val alarm: AlarmUiModel? = null,
    val style: PopUpStyle = PopUpStyle.DEFAULT,
    val isLoading: Boolean = true
)

sealed interface AlarmPopUpIntent {
    data class Init(
        val instanceId: Long,
        val label: String = "",
        val hour: Int = -1,
        val minute: Int = -1
    ) : AlarmPopUpIntent

    data object Snooze : AlarmPopUpIntent
    data object Dismiss : AlarmPopUpIntent
}

sealed interface AlarmPopUpEffect {
    data object FinishAndStopService : AlarmPopUpEffect
}
