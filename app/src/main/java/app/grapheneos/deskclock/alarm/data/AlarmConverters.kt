package app.grapheneos.deskclock.alarm.data

import androidx.room.TypeConverter

class AlarmConverters {
    @TypeConverter
    fun fromAlarmState(state: AlarmState): Int = state.value

    @TypeConverter
    fun toAlarmState(value: Int): AlarmState =
        AlarmState.entries.find { it.value == value } ?: AlarmState.SCHEDULED
}
