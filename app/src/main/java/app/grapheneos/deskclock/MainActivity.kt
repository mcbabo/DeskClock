package app.grapheneos.deskclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import app.grapheneos.deskclock.core.navigation.NavigationRoot
import app.grapheneos.deskclock.core.navigation.Route
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.data.ThemeMode
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(Route.Main)

            val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = AppSettings()
            )

            val isDarkTheme =
                when (settings.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

            DeskClockTheme(
                darkTheme = isDarkTheme,
                dynamicColor = settings.dynamicColors
            ) {
                SystemBarsTheme()
                NavigationRoot(backStack)
            }
        }
    }
}
