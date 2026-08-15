package app.grapheneos.deskclock.stopwatch.data

import android.content.Context
import android.os.SystemClock
import app.grapheneos.deskclock.core.util.startStopwatchService
import app.grapheneos.deskclock.core.util.stopStopwatchService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

data class StopwatchData(
    val isRunning: Boolean = false,
    val startTime: Long? = null,
    val accumulatedMillis: Long = 0L,
    val laps: List<Lap> = emptyList()
) {
    fun getElapsedMillis(now: Long = SystemClock.elapsedRealtime()): Long {
        return if (isRunning && startTime != null) {
            accumulatedMillis + (now - startTime)
        } else {
            accumulatedMillis
        }
    }
}

class StopwatchRepository(
    private val context: Context
) {
    private val _state = MutableStateFlow(StopwatchData())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val elapsedMillis: Flow<Long> = state.flatMapLatest { d ->
        if (d.isRunning) {
            flow {
                while (true) {
                    emit(d.getElapsedMillis())
                    delay(TICK_INTERVAL_MILLIS.milliseconds)
                }
            }
        } else {
            flowOf(d.accumulatedMillis)
        }
    }

    fun start() {
        _state.update { current ->
            if (current.isRunning) return@update current
            current.copy(
                isRunning = true,
                startTime = SystemClock.elapsedRealtime()
            )
        }
        context.startStopwatchService()
    }

    fun pause() {
        _state.update { current ->
            if (!current.isRunning) return@update current
            val now = SystemClock.elapsedRealtime()
            current.copy(
                isRunning = false,
                startTime = null,
                accumulatedMillis = current.getElapsedMillis(now)
            )
        }
    }

    fun reset() {
        _state.value = StopwatchData()
        context.stopStopwatchService()
    }

    fun lap() {
        _state.update { current ->
            if (!current.isRunning) return@update current
            val now = SystemClock.elapsedRealtime()
            val elapsed = current.getElapsedMillis(now)
            val previousTotal = current.laps.firstOrNull()?.totalMillis ?: 0L
            val newLap = Lap(
                number = current.laps.size + 1,
                splitMillis = elapsed - previousTotal,
                totalMillis = elapsed
            )
            current.copy(laps = listOf(newLap) + current.laps)
        }
    }

    companion object {
        private const val TICK_INTERVAL_MILLIS = 100L
    }
}
