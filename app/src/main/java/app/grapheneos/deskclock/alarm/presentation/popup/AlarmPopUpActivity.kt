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
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.collectEffectsOnStart
import app.grapheneos.deskclock.core.util.configureShowOnLockScreen
import app.grapheneos.deskclock.core.util.stopServiceAndFinish
import app.grapheneos.deskclock.settings.data.ThemeMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class AlarmPopUpActivity : ComponentActivity() {
    private val viewModel: AlarmPopUpViewModel by viewModel()

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
            val settings by viewModel.settings.collectAsStateWithLifecycle()

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

                    val uiState by viewModel.uiState.collectAsState()

                    AlarmPopUpScreen(
                        uiState = uiState,
                        onIntent = { action -> viewModel.handleIntent(action) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent) {
        val instanceId = intent.getLongExtra(Constants.Alarm.EXTRA_INSTANCE_ID, -1L)
        val label = intent.getStringExtra(Constants.Alarm.EXTRA_ALARM_LABEL) ?: ""
        val hour = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_HOUR, -1)
        val minute = intent.getIntExtra(Constants.Alarm.EXTRA_ALARM_MINUTE, -1)

        viewModel.handleIntent(AlarmPopUpIntent.Init(instanceId, label, hour, minute))
    }
}
