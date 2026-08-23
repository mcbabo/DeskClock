package app.grapheneos.deskclock.alarm.util

import java.time.DayOfWeek

/**
 * Utility for handling alarm day bitmasks and their mapping to [DayOfWeek].
 */
object AlarmDays {
    private const val BIT_SUN = 1 shl 0
    private const val BIT_MON = 1 shl 1
    private const val BIT_TUE = 1 shl 2
    private const val BIT_WED = 1 shl 3
    private const val BIT_THU = 1 shl 4
    private const val BIT_FRI = 1 shl 5
    private const val BIT_SAT = 1 shl 6

    private const val BIT_ALL_DAYS = 0x7F
    private const val BIT_WEEKDAYS = BIT_MON or BIT_TUE or BIT_WED or BIT_THU or BIT_FRI
    private const val BIT_WEEKEND = BIT_SAT or BIT_SUN

    /**
     * Converts a bitmask of days into a set of [DayOfWeek].
     */
    fun fromBitmask(bitmask: Int): Set<DayOfWeek> {
        return DayOfWeek.entries.filter { day ->
            (bitmask and (1 shl getShiftForDay(day))) != 0
        }.toSet()
    }

    /**
     * Converts a collection of [DayOfWeek] into a bitmask.
     */
    fun toBitmask(days: Collection<DayOfWeek>): Int {
        return days.fold(0) { mask, day ->
            mask or (1 shl getShiftForDay(day))
        }
    }

    /**
     * Toggles a specific [DayOfWeek] in the given bitmask.
     */
    fun toggleDay(bitmask: Int, day: DayOfWeek): Int {
        return bitmask xor (1 shl getShiftForDay(day))
    }

    /**
     * Checks if the bitmask represents all days of the week.
     */
    fun isAllDays(bitmask: Int): Boolean = (bitmask and BIT_ALL_DAYS) == BIT_ALL_DAYS

    /**
     * Checks if the bitmask represents weekdays (Mon-Fri).
     */
    fun isWeekdays(bitmask: Int): Boolean = (bitmask and BIT_ALL_DAYS) == BIT_WEEKDAYS

    /**
     * Checks if the bitmask represents weekends (Sat-Sun).
     */
    fun isWeekends(bitmask: Int): Boolean = (bitmask and BIT_ALL_DAYS) == BIT_WEEKEND

    /**
     * Maps [DayOfWeek] to the bit shift value used in the bitmask.
     * Sunday is 0, Monday is 1, ..., Saturday is 6.
     */
    private fun getShiftForDay(day: DayOfWeek): Int {
        return if (day == DayOfWeek.SUNDAY) 0 else day.value
    }
}
