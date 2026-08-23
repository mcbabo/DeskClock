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
        return when {
            daysOfWeek == 0 -> context.getString(R.string.one_time)
            AlarmDays.isAllDays(daysOfWeek) -> context.getString(R.string.daily)
            AlarmDays.isWeekdays(daysOfWeek) -> context.getString(R.string.mon_fri)
            AlarmDays.isWeekends(daysOfWeek) -> context.getString(R.string.sat_sun)
            else -> {
                val selectedDays = AlarmDays.fromBitmask(daysOfWeek)
                daysOrder.filter { it in selectedDays }
                    .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
            }
        }
    }

    fun getDayChipsState(selectedDaysBitmask: Int, locale: Locale): List<DayChipState> {
        val selectedDays = AlarmDays.fromBitmask(selectedDaysBitmask)
        return daysOrder.map { dayOfWeek ->
            DayChipState(
                label = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                shift = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value,
                isSelected = dayOfWeek in selectedDays
            )
        }
    }

    fun toggleDayInBitmask(currentBitmask: Int, shift: Int): Int {
        val day = DayOfWeek.entries.find { (if (it == DayOfWeek.SUNDAY) 0 else it.value) == shift }
        return if (day != null) AlarmDays.toggleDay(currentBitmask, day) else currentBitmask
    }
}
