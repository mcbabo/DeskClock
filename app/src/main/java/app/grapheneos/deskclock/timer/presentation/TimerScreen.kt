package app.grapheneos.deskclock.timer.presentation

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
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.timer.presentation.components.TimerKeypad
import app.grapheneos.deskclock.timer.util.TimerUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onIntent: (TimerIntent) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer)) },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
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
                        { onIntent(TimerIntent.TogglePauseResume) },
                        { onIntent(TimerIntent.Reset) }
                    )
                } else {
                    TimerSetupLayout(
                        uiState,
                        { digit -> onIntent(TimerIntent.EnterDigit(digit)) },
                        { onIntent(TimerIntent.Backspace) },
                        { onIntent(TimerIntent.Start) }
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
                imageVector = Icons.Outlined.PlayArrow,
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
            remainingTime = TimerUtils.formatRemainingTime(uiState.remainingTime),
            isRunning = uiState.isRunning
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
    remainingTime: String,
    isRunning: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TimerProgressAnimation"
    )

    val wavelength = WavyProgressIndicatorDefaults.CircularWavelength

    Box(contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(240.dp),
            amplitude = if (isRunning) {
                WavyProgressIndicatorDefaults.indicatorAmplitude
            } else {
                { 0f }
            },
            waveSpeed = if (isRunning) wavelength else 0.dp
        )
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Preview
@Composable
fun TimerSetupPreview() {
    DeskClockTheme {
        TimerScreen(
            uiState = TimerUiState(inputTime = "001000"),
            onIntent = {},
            onSettingsClick = {},
        )
    }
}

@Preview
@Composable
fun TimerRunningPreview() {
    DeskClockTheme {
        TimerScreen(
            uiState = TimerUiState(
                isStarted = true,
                isRunning = true,
                remainingTime = 45000,
                progress = 0.75f
            ),
            onIntent = {},
            onSettingsClick = {},
        )
    }
}
