package app.grapheneos.deskclock.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import app.grapheneos.deskclock.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object AlarmStylePicker : Route

    @Serializable
    data object TimerStylePicker : Route
}

val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("No NavBackStack provided")
}

enum class ClockTab(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    Alarm(R.string.tab_alarm, Icons.Outlined.Alarm, Icons.Filled.Alarm),
    WorldClock(R.string.tab_clock, Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
    Timer(R.string.tab_timer, Icons.Outlined.HourglassBottom, Icons.Filled.HourglassBottom),
    Stopwatch(R.string.tab_stopwatch, Icons.Outlined.Timer, Icons.Filled.Timer)
}
