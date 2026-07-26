package app.grapheneos.deskclock.stopwatch.util

enum class StopwatchPrecision {
    /** No milliseconds shown (00:00). */
    NONE,

    /** One digit of milliseconds shown (00:00.0). */
    TENTHS,

    /** Two digits of milliseconds shown (00:00.00). */
    CENTISECONDS
}
