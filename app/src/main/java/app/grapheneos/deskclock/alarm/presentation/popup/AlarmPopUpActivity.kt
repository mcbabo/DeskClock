package app.grapheneos.deskclock.alarm.presentation.popup

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.service.AlarmService
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.theme.SystemBarsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

class AlarmPopUpActivity : ComponentActivity(), KoinComponent {
    private val viewModel: AlarmPopUpViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureLockScreenProperties()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        AlarmEffect.FinishAndStopService -> handleTermination()
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            DeskClockTheme {
                SystemBarsTheme()

                val state by viewModel.uiState.collectAsState()

                AlarmPopUpScreen(
                    state = state,
                    onIntent = { intent -> viewModel.handleIntent(intent) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val instanceId = intent.getLongExtra(AlarmConstants.EXTRA_INSTANCE_ID, -1L)
        viewModel.handleIntent(AlarmPopUpAction.Init(instanceId))
    }

    private fun configureLockScreenProperties() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            LayoutParams.FLAG_KEEP_SCREEN_ON or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        keyguardManager.requestDismissKeyguard(this, null)
    }

    private fun handleTermination() {
        stopService(Intent(this, AlarmService::class.java))
        finish()
    }
}

@Composable
fun AlarmPopUpScreen(
    state: AlarmPopUpUiState,
    onIntent: (AlarmPopUpAction) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        AlarmPopUpContent(
            alarmWithInstance = state.alarmWithInstance,
            onDismiss = { onIntent(AlarmPopUpAction.Dismiss) },
            onSnooze = { onIntent(AlarmPopUpAction.Snooze) }
        )
    }
}

@Composable
fun AlarmPopUpContent(
    alarmWithInstance: AlarmWithInstance?,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)
            currentCalendar = Calendar.getInstance()
        }
    }

    val currentTimeText = String.format(
        locale,
        "%02d:%02d",
        currentCalendar.get(Calendar.HOUR_OF_DAY),
        currentCalendar.get(Calendar.MINUTE)
    )

    val scheduledTimeText = alarmWithInstance?.let {
        String.format(locale, "%02d:%02d", it.alarm.hour, it.alarm.minute)
    }

    val headerText = when {
        alarmWithInstance == null -> stringResource(R.string.alarm)
        alarmWithInstance.alarm.label.isNotEmpty() -> alarmWithInstance.alarm.label
        else -> stringResource(R.string.wake_up)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .screenPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (scheduledTimeText != null) {
                    Text(
                        text = stringResource(R.string.alarm_n, scheduledTimeText),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(Layout.RoundedCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.snooze),
                            style = MaterialTheme.typography.titleLarge
                        )
                        val minutes = alarmWithInstance?.alarm?.snoozeDurationMinutes
                            ?: AlarmConstants.DEFAULT_SNOOZE_TIME
                        Text(
                            text = stringResource(R.string.n_minutes, minutes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(Layout.RoundedCornerRadius)
                ) {
                    Text(
                        text = stringResource(R.string.dismiss),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AlarmPopUpContentPreview() {
    DeskClockTheme {
        AlarmPopUpContent(
            alarmWithInstance = AlarmWithInstance(
                alarm = AlarmEntity(
                    id = 1,
                    hour = 7,
                    minute = 30,
                    daysOfWeek = 0,
                    isEnabled = true,
                    deleteAfterUse = false,
                    label = ""
                ),
                instance = null
            ),
            onDismiss = {},
            onSnooze = {}
        )
    }
}
