package app.grapheneos.deskclock.timer.util

import kotlin.math.abs

/**
 * Utility for formatting and parsing timer-related time values.
 */
object TimerUtils {

    /**
     * Formats a 6-digit input string (HHMMSS) into a human-readable "Hh Mm Ss" string.
     */
    fun formatInputTime(input: String): String {
        val clean = input.padStart(6, '0')
        val h = clean.substring(0, 2)
        val m = clean.substring(2, 4)
        val s = clean.substring(4, 6)
        return "${h}h ${m}m ${s}s"
    }

    /**
     * Formats a duration in milliseconds into a "HH:MM:SS" or "MM:SS" string.
     */
    fun formatRemainingTime(millis: Long): String {
        val isNegative = millis < 0
        val totalSeconds = abs(millis) / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        val hStr = h.toString().padStart(2, '0')
        val mStr = m.toString().padStart(2, '0')
        val sStr = s.toString().padStart(2, '0')

        val timeString = if (h > 0) "$hStr:$mStr:$sStr" else "$mStr:$sStr"
        return if (isNegative) "-$timeString" else timeString
    }

    /**
     * Parses a 6-digit input string (HHMMSS) into total milliseconds.
     */
    fun parseInputToMillis(input: String): Long {
        val clean = input.padStart(6, '0')
        val hours = clean.substring(0, 2).toIntOrNull() ?: 0
        val minutes = clean.substring(2, 4).toIntOrNull() ?: 0
        val seconds = clean.substring(4, 6).toIntOrNull() ?: 0
        return ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
    }

    /**
     * Formats total milliseconds into a 6-digit input string (HHMMSS).
     */
    fun formatMillisToInput(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val h = (totalSeconds / 3600).coerceAtMost(99)
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return buildString {
            append(h.toString().padStart(2, '0'))
            append(m.toString().padStart(2, '0'))
            append(s.toString().padStart(2, '0'))
        }
    }
}
