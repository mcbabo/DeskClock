package app.grapheneos.deskclock.stopwatch.util

/**
 * Formats a duration in milliseconds into a human-readable stopwatch string.
 *
 * @param elapsedMillis The duration to format.
 * @param precision The [StopwatchPrecision] to use for the output.
 * @return A formatted string (e.g., "01:23.4" or "02:03.45").
 */
fun formatStopwatchTime(
    elapsedMillis: Long,
    precision: StopwatchPrecision = StopwatchPrecision.CENTISECONDS
): String {
    val time = elapsedMillis.coerceAtLeast(0L)
    val hours = time / 3_600_000
    val minutes = (time % 3_600_000) / 60_000
    val seconds = (time % 60_000) / 1_000
    val millis = (time % 1_000)

    return buildString {
        if (hours > 0) {
            append(hours).append(':')
            append(minutes.toString().padStart(2, '0')).append(':')
        } else {
            append(minutes.toString().padStart(2, '0')).append(':')
        }

        append(seconds.toString().padStart(2, '0'))

        when (precision) {
            StopwatchPrecision.TENTHS -> {
                append('.').append(millis / 100)
            }

            StopwatchPrecision.CENTISECONDS -> {
                append('.').append((millis / 10).toString().padStart(2, '0'))
            }

            StopwatchPrecision.NONE -> {}
        }
    }
}

fun formatLapTime(millis: Long): String =
    formatStopwatchTime(millis, StopwatchPrecision.CENTISECONDS)
