package app.grapheneos.deskclock.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import app.grapheneos.deskclock.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route, NavKey

    @Serializable
    data object Settings : Route, NavKey
}

enum class ClockTab(@StringRes val titleRes: Int, val icon: ImageVector) {
    Alarm(R.string.tab_alarm, Icons.Outlined.Alarm),
    WorldClock(R.string.tab_clock, Icons.Outlined.AccessTime),
    Timer(R.string.tab_timer, Icons.Outlined.HourglassBottom),
    Stopwatch(R.string.tab_stopwatch, Icons.Outlined.Timer)
}
