package app.grapheneos.deskclock.timer.data

import android.os.SystemClock

data class TimerData(
    val inputTime: String = "000000",
    val totalMillis: Long = 0L,
    val remainingTimeAtStart: Long = 0L,
    val startTime: Long? = null,
    val isStarted: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
) {
    fun getRemainingTime(now: Long = SystemClock.elapsedRealtime()): Long {
        return if (startTime != null) {
            remainingTimeAtStart - (now - startTime)
        } else {
            remainingTimeAtStart
        }
    }

    val progress: Float
        get() = if (totalMillis > 0) {
            (getRemainingTime().toFloat() / totalMillis).coerceIn(0f, 1f)
        } else {
            0f
        }
}
