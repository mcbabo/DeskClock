package app.grapheneos.deskclock.alarm.util

import android.content.Context
import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Immutable
data class DayChipState(
    val label: String,
    val shift: Int,
    val isSelected: Boolean
)

object AlarmDayFormatter {
    private val daysOrder = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    fun formatDaysOfWeek(context: Context, daysOfWeek: Int): String {
        val locale = context.resources.configuration.locales[0]
        val dayBits = daysOfWeek and AlarmConstants.BIT_ALL_DAYS
        return when (dayBits) {
            0 -> context.getString(R.string.one_time)
            AlarmConstants.BIT_ALL_DAYS -> context.getString(R.string.daily)
            AlarmConstants.BIT_WEEKDAYS -> context.getString(R.string.mon_fri)
            AlarmConstants.BIT_WEEKEND -> context.getString(R.string.sat_sun)
            else -> {
                val selectedDays = mutableListOf<String>()
                daysOrder.forEach { dayOfWeek ->
                    val shift = getShiftForDay(dayOfWeek)
                    if ((daysOfWeek and (1 shl shift)) != 0) {
                        selectedDays.add(dayOfWeek.getDisplayName(TextStyle.SHORT, locale))
                    }
                }
                selectedDays.joinToString(", ")
            }
        }
    }

    fun getDayChipsState(selectedDaysBitmask: Int, locale: Locale): List<DayChipState> {
        return daysOrder.map { dayOfWeek ->
            val shift = getShiftForDay(dayOfWeek)
            DayChipState(
                label = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                shift = shift,
                isSelected = (selectedDaysBitmask and (1 shl shift)) != 0
            )
        }
    }

    fun toggleDayInBitmask(currentBitmask: Int, shift: Int): Int {
        return currentBitmask xor (1 shl shift)
    }

    private fun getShiftForDay(dayOfWeek: DayOfWeek): Int {
        return if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
    }
}
