package app.grapheneos.deskclock.stopwatch.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import app.grapheneos.deskclock.stopwatch.util.StopwatchPrecision
import app.grapheneos.deskclock.stopwatch.util.formatStopwatchTime
import kotlinx.coroutines.flow.Flow

@Composable
fun StopwatchTimeText(
    elapsedMillisFlow: Flow<Long>,
    initialMillis: Long,
    isRunning: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val elapsedMillis by elapsedMillisFlow.collectAsState(initial = initialMillis)
    StopwatchTimeText(
        elapsedMillis = elapsedMillis,
        isRunning = isRunning,
        style = style,
        modifier = modifier
    )
}

@Composable
fun StopwatchTimeText(
    elapsedMillis: Long,
    isRunning: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val precision = if (isRunning) {
        StopwatchPrecision.TENTHS
    } else {
        StopwatchPrecision.CENTISECONDS
    }

    Text(
        text = formatStopwatchTime(elapsedMillis, precision),
        style = style,
        modifier = modifier
    )
}
