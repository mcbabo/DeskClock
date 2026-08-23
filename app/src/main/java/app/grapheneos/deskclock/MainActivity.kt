package app.grapheneos.deskclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    private val actionState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionState.value = intent?.action
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(Route.Main)

            settings?.let {
                DeskClockAppTheme(settings = it) {
                    SystemBarsTheme()
                    NavigationRoot(backStack, initialAction = actionState.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        actionState.value = intent.action
    }
}
