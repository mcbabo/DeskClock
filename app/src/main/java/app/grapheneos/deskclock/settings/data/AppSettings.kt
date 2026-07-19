package app.grapheneos.deskclock.settings.data

import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = false,

    val snoozeDurationMinutes: Int = AlarmConstants.DEFAULT_SNOOZE_TIME,
    val defaultRingtone: RingtoneItem = RingtoneItem(
        "Cesium",
        "content://media/internal/audio/media/138?title=Cesium&canonical=1"
    ),
    val vibrate: Boolean = true,

    val stopwatchShowMilliseconds: Boolean = true,
)

@Serializable
enum class ThemeMode(val displayName: String, val symbol: Boolean?) {
    LIGHT("Light", false),
    DARK("Dark", true),
    SYSTEM("System Default", null)
}
