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
import app.grapheneos.deskclock.core.presentation.MainActivityViewModel
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import app.grapheneos.deskclock.settings.data.ThemeMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(Route.Main)

            if (settings != null) {
                val isDarkTheme =
                    when (settings!!.themeMode) {
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                        ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    }

                DeskClockTheme(
                    darkTheme = isDarkTheme,
                    dynamicColor = settings!!.dynamicColors
                ) {
                    SystemBarsTheme()
                    NavigationRoot(backStack)
                }
            }
        }
    }
}
