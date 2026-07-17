package app.grapheneos.deskclock.alarm.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import app.grapheneos.deskclock.alarm.util.AlarmDayFormatter
import app.grapheneos.deskclock.core.presentation.Layout

@Composable
fun DayChips(
    selectedDaysBitmask: Int,
    onBitmaskChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLocale = LocalLocale.current.platformLocale
    val chipStates = AlarmDayFormatter.getDayChipsState(selectedDaysBitmask, currentLocale)
    val rowState = rememberLazyListState()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(Layout.ChipSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(chipStates) { chip ->
            FilterChip(
                selected = chip.isSelected,
                onClick = {
                    val newBitmask =
                        AlarmDayFormatter.toggleDayInBitmask(selectedDaysBitmask, chip.shift)
                    onBitmaskChange(newBitmask)
                },
                label = { Text(chip.label) },
                leadingIcon = if (chip.isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}
