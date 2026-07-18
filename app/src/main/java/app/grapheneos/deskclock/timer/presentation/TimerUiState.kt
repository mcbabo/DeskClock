package app.grapheneos.deskclock.timer.presentation

data class TimerUiState(
    val remainingTime: Long = 0L,
    val totalTime: Long = 0L,
    val inputTime: String = "0000000", // HHMMSS
    val isStarted: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val progress: Float = 0f
)

sealed interface TimerAction {
    data class EnterDigit(val digit: Int) : TimerAction
    object Backspace : TimerAction
    object Start : TimerAction
    object TogglePauseResume : TimerAction
    object Reset : TimerAction
}
