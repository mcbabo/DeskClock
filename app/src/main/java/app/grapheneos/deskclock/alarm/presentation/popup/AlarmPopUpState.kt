package app.grapheneos.deskclock.alarm.presentation.popup

import app.grapheneos.deskclock.alarm.data.AlarmWithInstance

data class AlarmPopUpUiState(
    val alarmWithInstance: AlarmWithInstance? = null,
    val isLoading: Boolean = true
)

sealed interface AlarmPopUpAction {
    data class Init(val instanceId: Long) : AlarmPopUpAction
    object Snooze : AlarmPopUpAction
    object Dismiss : AlarmPopUpAction
}

sealed interface AlarmEffect {
    object FinishAndStopService : AlarmEffect
}
