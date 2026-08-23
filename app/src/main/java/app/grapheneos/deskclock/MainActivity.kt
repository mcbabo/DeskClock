package app.grapheneos.deskclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import app.grapheneos.deskclock.core.navigation.NavigationRoot
import app.grapheneos.deskclock.core.navigation.Route
import app.grapheneos.deskclock.core.presentation.MainActivityViewModel
import app.grapheneos.deskclock.core.theme.DeskClockAppTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.onNewIntentAction(intent?.action)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(Route.Main)

            uiState.settings?.let { settings ->
                DeskClockAppTheme(settings = settings) {
                    SystemBarsTheme()
                    NavigationRoot(backStack, initialAction = uiState.initialAction)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onNewIntentAction(intent.action)
    }
}
