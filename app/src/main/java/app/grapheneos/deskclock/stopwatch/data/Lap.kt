package app.grapheneos.deskclock.stopwatch.data

data class Lap(
    val number: Int,
    val splitMillis: Long,
    val totalMillis: Long
)
