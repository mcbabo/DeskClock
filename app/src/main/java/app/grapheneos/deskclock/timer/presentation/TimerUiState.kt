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

sealed interface TimerIntent {
    data class EnterDigit(val digit: Int) : TimerIntent
    object Backspace : TimerIntent
    object Start : TimerIntent
    object TogglePauseResume : TimerIntent
    object Reset : TimerIntent
}
