package app.grapheneos.deskclock.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.settings.presentation.AppSettingsUiModel

/**
 * A shared Composable wrapper that determines the app's theme based on the user's
 * settings (Light, Dark, or System Default) and applies the [DeskClockTheme].
 */
@Composable
fun DeskClockAppTheme(
    settings: AppSettingsUiModel,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    DeskClockTheme(
        darkTheme = isDarkTheme,
        dynamicColor = settings.dynamicColors,
        content = content
    )
}
