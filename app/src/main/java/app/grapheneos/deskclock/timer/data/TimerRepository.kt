package app.grapheneos.deskclock.timer.data

import app.grapheneos.deskclock.timer.presentation.TimerUiState
import app.grapheneos.deskclock.timer.service.TimerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TimerRepository(
    private val timerController: TimerController
) {
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState = _timerState.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    fun start() {
        if (_timerState.value.isRunning) return

        _timerState.update { it.copy(isStarted = true, isRunning = true) }
        timerController.startTimerService()

        timerJob?.cancel()
        timerJob = repositoryScope.launch {
            while (_timerState.value.isStarted || _timerState.value.isFinished) {
                delay(1000.milliseconds)
                _timerState.update { current ->
                    val nextRemaining = current.remainingTime - 1000L

                    current.copy(
                        remainingTime = nextRemaining,
                        progress = if (nextRemaining > 0) nextRemaining.toFloat() / current.totalTime else 0f,
                        isFinished = nextRemaining <= 0
                    )
                }
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _timerState.update { current ->
            val parsedTimeMs = parseInputToMillis(current.inputTime)
            TimerUiState(
                inputTime = current.inputTime,
                remainingTime = parsedTimeMs,
                totalTime = parsedTimeMs,
                isStarted = false,
                isRunning = false,
                progress = 1.0f
            )
        }
    }

    fun togglePauseResume() {
        if (_timerState.value.isRunning) pause() else start()
    }

    fun enterDigit(digit: Int) {
        _timerState.update { current ->
            val currentInput = current.inputTime.replaceFirst("^0+".toRegex(), "")
            if (currentInput.length >= 6) return
            val newInput = (currentInput + digit).padStart(6, '0')
            val parsedTimeMs = parseInputToMillis(newInput)
            current.copy(
                inputTime = newInput,
                remainingTime = parsedTimeMs,
                totalTime = parsedTimeMs,
                progress = 1.0f
            )
        }
    }

    fun backspace() {
        _timerState.update { current ->
            val newInput = ("0" + current.inputTime.dropLast(1)).padStart(6, '0')
            val parsedTimeMs = parseInputToMillis(newInput)
            current.copy(
                inputTime = newInput,
                remainingTime = parsedTimeMs,
                totalTime = parsedTimeMs,
                progress = 1.0f
            )
        }
    }

    private fun parseInputToMillis(input: String): Long {
        val cleanInput = input.padStart(6, '0')
        val hours = cleanInput.substring(0, 2).toIntOrNull() ?: 0
        val minutes = cleanInput.substring(2, 4).toIntOrNull() ?: 0
        val seconds = cleanInput.substring(4, 6).toIntOrNull() ?: 0
        return ((hours * 3600) + (minutes * 60) + seconds) * 1000L
    }
}
