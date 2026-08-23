package app.grapheneos.deskclock.alarm.util

import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object AlarmTimeCalculator {
    fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: Int): Long {
        val now = ZonedDateTime.now()
        val targetTime = LocalTime.of(hour, minute)

        if (daysOfWeek == 0) {
            val triggerToday = now.with(targetTime).withSecond(0).withNano(0)
            return (if (triggerToday.isAfter(now)) triggerToday else triggerToday.plusDays(1))
                .toInstant()
                .toEpochMilli()
        }

        return AlarmDays.fromBitmask(daysOfWeek).asSequence()
            .map { day ->
                val trigger = now.with(TemporalAdjusters.nextOrSame(day))
                    .with(targetTime)
                    .withSecond(0)
                    .withNano(0)
                if (trigger.isBefore(now)) trigger.plusWeeks(1) else trigger
            }
            .minOf { it }
            .toInstant()
            .toEpochMilli()
    }
}
