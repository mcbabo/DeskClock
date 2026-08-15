package app.grapheneos.deskclock.timer.data

import android.content.Context
import android.os.SystemClock
import app.grapheneos.deskclock.core.util.startTimerService
import app.grapheneos.deskclock.core.util.stopTimerService
import app.grapheneos.deskclock.timer.util.TimerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Data layer for managing the state and lifecycle of a Timer.
 */
class TimerRepository(
    private val context: Context
) {
    private val _state = MutableStateFlow(TimerData())
    val state = _state.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var finishJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val remainingMillis: Flow<Long> = state.flatMapLatest { d ->
        if (d.isRunning) {
            flow {
                while (true) {
                    emit(d.getRemainingTime())
                    delay(100.milliseconds)
                }
            }
        } else {
            flowOf(d.getRemainingTime())
        }
    }

    fun start() {
        _state.update { current ->
            if (current.isRunning) return@update current
            val remaining = current.getRemainingTime()
            if (remaining <= 0 && !current.isFinished) return@update current

            current.copy(
                isStarted = true,
                isRunning = true,
                startTime = SystemClock.elapsedRealtime(),
                remainingTimeAtStart = remaining,
                totalMillis = if (current.totalMillis == 0L) remaining else current.totalMillis,
                isFinished = false
            )
        }
        context.startTimerService()
        scheduleFinish()
    }

    private fun scheduleFinish() {
        finishJob?.cancel()
        val current = _state.value
        if (!current.isRunning || current.isFinished) return

        val remaining = current.getRemainingTime()
        finishJob = repositoryScope.launch {
            delay(remaining.milliseconds)
            _state.update {
                it.copy(
                    isFinished = true
                )
            }
        }
    }

    fun pause() {
        finishJob?.cancel()
        _state.update { current ->
            if (!current.isRunning) return@update current
            current.copy(
                isRunning = false,
                startTime = null,
                remainingTimeAtStart = current.getRemainingTime()
            )
        }
    }

    fun reset() {
        finishJob?.cancel()
        _state.update { current ->
            val parsedTimeMs = TimerUtils.parseInputToMillis(current.inputTime)
            TimerData(
                inputTime = current.inputTime,
                totalMillis = parsedTimeMs,
                remainingTimeAtStart = parsedTimeMs,
                isStarted = false,
                isFinished = false
            )
        }
        context.stopTimerService()
    }

    fun togglePauseResume() {
        if (_state.value.isRunning) pause() else start()
    }

    fun addTime(millis: Long) {
        _state.update { current ->
            val newRemaining = current.getRemainingTime() + millis
            val newInput = TimerUtils.formatMillisToInput(newRemaining)
            current.copy(
                remainingTimeAtStart = newRemaining,
                totalMillis = current.totalMillis + millis,
                startTime = if (current.isRunning) SystemClock.elapsedRealtime() else current.startTime,
                inputTime = newInput,
                isFinished = false
            )
        }
        if (_state.value.isRunning) {
            scheduleFinish()
        }
    }

    fun enterDigit(digit: Int) {
        _state.update { current ->
            val currentInput = current.inputTime.replaceFirst("^0+".toRegex(), "")
            if (currentInput.length >= 6) return@update current
            val newInput = (currentInput + digit).padStart(6, '0')
            val parsedTimeMs = TimerUtils.parseInputToMillis(newInput)
            current.copy(
                inputTime = newInput,
                totalMillis = parsedTimeMs,
                remainingTimeAtStart = parsedTimeMs,
                isStarted = false,
                isFinished = false
            )
        }
    }

    fun backspace() {
        _state.update { current ->
            val newInput = ("0" + current.inputTime.dropLast(1)).padStart(6, '0')
            val parsedTimeMs = TimerUtils.parseInputToMillis(newInput)
            current.copy(
                inputTime = newInput,
                totalMillis = parsedTimeMs,
                remainingTimeAtStart = parsedTimeMs,
                isStarted = false,
                isFinished = false
            )
        }
    }
}
