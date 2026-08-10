package app.grapheneos.deskclock.timer.presentation.popup.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.PopUpButton
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun TimerPopUpVariant(
    remainingTime: Long,
    onStop: () -> Unit,
    onAddMinute: () -> Unit
) {
    TimerPopUpBase(
        remainingTime = remainingTime
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PopUpButton(
                onClick = onAddMinute,
                icon = Icons.Default.Add,
                text = stringResource(R.string.add_minute),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            PopUpButton(
                onClick = onStop,
                icon = Icons.Default.Stop,
                text = stringResource(R.string.stop),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Preview
@Composable
fun TimerPopUpVariantPreview() {
    DeskClockTheme {
        TimerPopUpVariant(remainingTime = 0L, onStop = {}, onAddMinute = {})
    }
}
