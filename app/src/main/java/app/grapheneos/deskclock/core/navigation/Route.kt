package app.grapheneos.deskclock.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route, NavKey

    @Serializable
    data object Settings : Route
}

enum class ClockTab(val title: String, val icon: ImageVector) {
    Alarm("Alarm", Icons.Outlined.Alarm),
    WorldClock("Clock", Icons.Outlined.AccessTime),
    Timer("Timer", Icons.Outlined.HourglassBottom),
    Stopwatch("Stopwatch", Icons.Outlined.Timer)
}
