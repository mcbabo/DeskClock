package app.grapheneos.deskclock.timer.presentation.popup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.timer.util.TimerUtils

@Composable
fun TimerPopUpScreen(
    uiState: TimerPopUpUiState,
    onIntent: (TimerPopUpIntent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .screenPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.timer_finished),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = TimerUtils.formatRemainingTime(uiState.remainingTime),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { onIntent(TimerPopUpIntent.Stop) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.stop),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
