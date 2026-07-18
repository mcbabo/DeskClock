package app.grapheneos.deskclock.timer.presentation.popup

data class TimerPopUpUiState(
    val remainingTime: Long = 0L
)

sealed interface TimerPopUpAction {
    object Stop : TimerPopUpAction
}

sealed interface TimerPopUpEffect {
    object Finish : TimerPopUpEffect
}
