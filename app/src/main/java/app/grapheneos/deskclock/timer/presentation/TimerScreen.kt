package app.grapheneos.deskclock.timer.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.timer.presentation.components.TimerKeypad
import app.grapheneos.deskclock.timer.util.TimerUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun TimerScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    TimerContent(
        uiState = uiState,
        onNavigateToSettings = onNavigateToSettings,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerContent(
    uiState: TimerUiState,
    onNavigateToSettings: () -> Unit,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = uiState.isStarted || uiState.isFinished,
                label = "TimerMode"
            ) { isStarted ->
                if (isStarted) {
                    TimerRunningLayout(
                        uiState,
                        { onAction(TimerAction.TogglePauseResume) },
                        { onAction(TimerAction.Reset) }
                    )
                } else {
                    TimerSetupLayout(
                        uiState,
                        { digit -> onAction(TimerAction.EnterDigit(digit)) },
                        { onAction(TimerAction.Backspace) },
                        { onAction(TimerAction.Start) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerSetupLayout(
    uiState: TimerUiState,
    onDigitClick: (Int) -> Unit,
    onBackspace: () -> Unit,
    onStartClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = TimerUtils.formatInputTime(uiState.inputTime),
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        TimerKeypad(
            onDigitClick = onDigitClick,
            onBackspace = onBackspace
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier.size(80.dp),
            enabled = uiState.inputTime.toLong() > 0
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.start)
            )
        }
    }
}

@Composable
private fun TimerRunningLayout(
    uiState: TimerUiState,
    onPauseResumeClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerIndicator(
            progress = uiState.progress,
            remainingTime = TimerUtils.formatRemainingTime(uiState.remainingTime)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            OutlinedButton(onClick = onResetClick) {
                Text(
                    text = stringResource(R.string.reset)
                )
            }
            Button(onClick = onPauseResumeClick) {
                Text(
                    text = if (uiState.isRunning) {
                        stringResource(R.string.pause)
                    } else {
                        stringResource(R.string.resume)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerIndicator(
    progress: Float,
    remainingTime: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TimerProgressAnimation"
    )

    Box(contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(240.dp)
        )
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.displayMedium
        )
    }
}
