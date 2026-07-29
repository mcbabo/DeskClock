package app.grapheneos.deskclock.timer.presentation.popup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import app.grapheneos.deskclock.core.util.collectEffectsOnStart
import app.grapheneos.deskclock.core.util.configureShowOnLockScreen
import app.grapheneos.deskclock.core.util.stopServiceAndFinish
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.timer.service.TimerService
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class TimerPopUpActivity : ComponentActivity(), KoinComponent {
    private val viewModel: TimerPopUpViewModel by viewModel()
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureShowOnLockScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        collectEffectsOnStart(viewModel.effect) { effect ->
            when (effect) {
                TimerPopUpEffect.Finish -> stopServiceAndFinish(TimerService::class.java)
            }
        }

        setContent {
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
                val uiState by viewModel.uiState.collectAsState()
                TimerPopUpScreen(uiState) { viewModel.handleIntent(it) }
            }
        }
    }
}
