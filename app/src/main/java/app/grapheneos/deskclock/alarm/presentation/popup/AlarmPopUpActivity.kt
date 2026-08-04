package app.grapheneos.deskclock.alarm.presentation.popup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.alarm.service.AlarmService
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import app.grapheneos.deskclock.core.util.collectEffectsOnStart
import app.grapheneos.deskclock.core.util.configureShowOnLockScreen
import app.grapheneos.deskclock.core.util.stopServiceAndFinish
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.data.ThemeMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class AlarmPopUpActivity : ComponentActivity(), KoinComponent {
    private val viewModel: AlarmPopUpViewModel by viewModel()
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureShowOnLockScreen()
        super.onCreate(savedInstanceState)

        handleAlarmIntent(intent)

        collectEffectsOnStart(viewModel.effect) { effect ->
            when (effect) {
                AlarmPopUpEffect.FinishAndStopService -> stopServiceAndFinish(AlarmService::class.java)
            }
        }

        enableEdgeToEdge()
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

                AlarmPopUpScreen(
                    uiState = uiState,
                    onIntent = { action -> viewModel.handleIntent(action) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent) {
        val instanceId = intent.getLongExtra(AlarmConstants.EXTRA_INSTANCE_ID, -1L)
        val label = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_LABEL) ?: ""
        val hour = intent.getIntExtra(AlarmConstants.EXTRA_ALARM_HOUR, -1)
        val minute = intent.getIntExtra(AlarmConstants.EXTRA_ALARM_MINUTE, -1)

        viewModel.handleIntent(AlarmPopUpIntent.Init(instanceId, label, hour, minute))
    }
}
