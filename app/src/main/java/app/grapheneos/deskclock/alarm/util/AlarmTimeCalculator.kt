package app.grapheneos.deskclock.alarm.util

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object AlarmTimeCalculator {
    fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeekBitmask: Int): Long {
        val now = ZonedDateTime.now()
        val alarmTimeToday = now.with(LocalTime.of(hour, minute, 0, 0))

        if (daysOfWeekBitmask == 0) {
            val result =
                if (alarmTimeToday.isBefore(now)) alarmTimeToday.plusDays(1) else alarmTimeToday
            return result.toInstant().toEpochMilli()
        }

        var nearest: ZonedDateTime? = null
        for (day in DayOfWeek.entries) {
            val shift = if (day == DayOfWeek.SUNDAY) 0 else day.value
            if ((daysOfWeekBitmask and (1 shl shift)) != 0) {
                var candidate = now.with(TemporalAdjusters.nextOrSame(day))
                    .with(LocalTime.of(hour, minute, 0, 0))

                if (candidate.isBefore(now)) candidate = candidate.plusWeeks(1)

                if (nearest == null || candidate.isBefore(nearest)) nearest = candidate
            }
        }
        return nearest!!.toInstant().toEpochMilli()
    }
}
