package app.grapheneos.deskclock.settings.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,

    val snoozeDurationMinutes: Int = AlarmConstants.DEFAULT_SNOOZE_TIME,
    val defaultRingtone: RingtoneItem = RingtoneItem(
        "Cesium",
        "content://media/internal/audio/media/138?title=Cesium&canonical=1"
    ),
    val vibrate: Boolean = true,

    val stopwatchShowMilliseconds: Boolean = true,
)

@Serializable
enum class ThemeMode(val displayName: String, val icon: ImageVector) {
    LIGHT("Light", Icons.Outlined.LightMode),
    DARK("Dark", Icons.Outlined.DarkMode),
    SYSTEM("System Default", Icons.Outlined.Smartphone)
}
