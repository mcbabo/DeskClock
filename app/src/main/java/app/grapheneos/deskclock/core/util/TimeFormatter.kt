package app.grapheneos.deskclock.core.util

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatSystemTime(context: Context, hour: Int, minute: Int): String {
    val localTime = LocalTime.of(hour, minute)
    val locale = Locale.getDefault()

    val is24Hour = DateFormat.is24HourFormat(context)
    val skeleton = if (is24Hour) "Hm" else "hm"

    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)

    return localTime.format(formatter)
}
