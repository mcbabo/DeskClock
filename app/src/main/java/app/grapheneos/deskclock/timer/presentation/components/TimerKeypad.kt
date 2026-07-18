package app.grapheneos.deskclock.timer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimerKeypad(onDigitClick: (Int) -> Unit, onBackspace: () -> Unit) {
    val digits = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, null, 0, -1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        digits.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { digit ->
                    when (digit) {
                        null -> Spacer(modifier = Modifier.size(64.dp))
                        -1 -> IconButton(
                            onClick = onBackspace,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = null
                            )
                        }

                        else -> TextButton(
                            onClick = { onDigitClick(digit) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Text(
                                text = digit.toString(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
