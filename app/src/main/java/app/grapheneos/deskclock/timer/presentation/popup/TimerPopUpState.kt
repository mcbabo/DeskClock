package app.grapheneos.deskclock.timer.presentation.popup

data class TimerPopUpUiState(
    val remainingTime: Long = 0L
)

sealed interface TimerPopUpIntent {
    object Stop : TimerPopUpIntent
}

sealed interface TimerPopUpEffect {
    object Finish : TimerPopUpEffect
}
