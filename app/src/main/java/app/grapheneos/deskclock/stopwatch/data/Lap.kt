package app.grapheneos.deskclock.stopwatch.data

import androidx.compose.runtime.Immutable

@Immutable
data class Lap(
    val number: Int,
    val splitMillis: Long,
    val totalMillis: Long
)
