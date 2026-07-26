package app.grapheneos.deskclock.stopwatch.data

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StopwatchData(
    val isRunning: Boolean = false,
    val startTime: Long? = null,
    val accumulatedMillis: Long = 0L,
    val laps: List<Lap> = emptyList()
) {
    /**
     * Calculates the total elapsed time.
     * If running, it adds the time since [startTime] to [accumulatedMillis].
     */
    fun getElapsedMillis(now: Long = SystemClock.elapsedRealtime()): Long {
        return if (isRunning && startTime != null) {
            accumulatedMillis + (now - startTime)
        } else {
            accumulatedMillis
        }
    }
}

class StopwatchRepository {
    private val _state = MutableStateFlow(StopwatchData())
    val state = _state.asStateFlow()

    fun start() {
        _state.update { current ->
            if (current.isRunning) return@update current
            current.copy(
                isRunning = true,
                startTime = SystemClock.elapsedRealtime()
            )
        }
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
}
