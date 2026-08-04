package app.grapheneos.deskclock.alarm.presentation.popup

import app.grapheneos.deskclock.alarm.data.AlarmWithInstance

data class AlarmPopUpUiState(
    val alarmWithInstance: AlarmWithInstance? = null,
    val isLoading: Boolean = true
)

sealed interface AlarmPopUpIntent {
    data class Init(
        val instanceId: Long,
        val label: String = "",
        val hour: Int = -1,
        val minute: Int = -1
    ) : AlarmPopUpIntent
    object Snooze : AlarmPopUpIntent
    object Dismiss : AlarmPopUpIntent
}

sealed interface AlarmPopUpEffect {
    object FinishAndStopService : AlarmPopUpEffect
}
