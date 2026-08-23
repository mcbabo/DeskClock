package app.grapheneos.deskclock.settings.data

import androidx.core.net.toUri
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
        "content://media/internal/audio/media/138?title=Cesium&canonical=1".toUri()
    ),
    val directBootRingtone: RingtoneItem = RingtoneItem(
        "Cesium",
        "android.resource://app.grapheneos.deskclock/raw/cesium".toUri()
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
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@Serializable
enum class PopUpStyle {
    DEFAULT,
    VARIANT,
    TERTIARY
}
