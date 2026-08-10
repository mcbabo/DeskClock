package app.grapheneos.deskclock.timer.util

import java.util.Locale

object TimerUtils {
    fun formatInputTime(input: String): String {
        val h = input.substring(0, 2)
        val m = input.substring(2, 4)
        val s = input.substring(4, 6)
        return "${h}h ${m}m ${s}s"
    }

    fun formatRemainingTime(millis: Long): String {
        val isNegative = millis < 0
        val absMillis = kotlin.math.abs(millis)

        val totalSeconds = absMillis / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        val timeString = if (h > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }

        return if (isNegative) "-$timeString" else timeString
    }

    fun parseInputToMillis(input: String): Long {
        val cleanInput = input.padStart(6, '0')
        val hours = cleanInput.substring(0, 2).toIntOrNull() ?: 0
        val minutes = cleanInput.substring(2, 4).toIntOrNull() ?: 0
        val seconds = cleanInput.substring(4, 6).toIntOrNull() ?: 0
        return ((hours * 3_600) + (minutes * 60) + seconds) * 1_000L
    }

    fun formatMillisToInput(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        // Locale.US only for internal calculation
        return String.format(Locale.US, "%02d%02d%02d", h.coerceAtMost(99), m, s)
    }
}
