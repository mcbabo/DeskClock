package app.grapheneos.deskclock.settings.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,

    val snoozeDurationMinutes: Int = Constants.Alarm.DEFAULT_SNOOZE_TIME,
    val defaultRingtone: RingtoneItem = RingtoneItem(
        "Cesium",
        "content://media/internal/audio/media/138?title=Cesium&canonical=1"
    ),
    val useCustomRingtoneVolume: Boolean = false,
    val ringtoneVolume: Float = 0.5F,
    val vibrate: Boolean = true,

    val stopwatchShowMilliseconds: Boolean = true,

    val alarmPopUpStyle: PopUpStyle = PopUpStyle.DEFAULT,
    val timerPopUpStyle: PopUpStyle = PopUpStyle.DEFAULT,

    val graduallyIncreaseVolume: Boolean = false,
    val graduallyIncreaseVolumeDuration: Int = 30,
)

@Serializable
enum class ThemeMode(@StringRes val displayNameRes: Int, val icon: ImageVector) {
    LIGHT(R.string.settings_light_mode, Icons.Outlined.LightMode),
    DARK(R.string.settings_dark_mode, Icons.Outlined.DarkMode),
    SYSTEM(R.string.settings_system_mode, Icons.Outlined.Smartphone)
}

@Serializable
enum class PopUpStyle(@StringRes val titleRes: Int) {
    DEFAULT(R.string.style_default),
    VARIANT(R.string.style_variant),
    TERTIARY(R.string.style_tertiary)
}
