package app.grapheneos.deskclock.clock.presentation

import java.time.ZoneId

data class TimeState(
    val localTime: String = "--:--:--",
    val localDate: String = ""
)

data class ClockScreenState(
    val zoneClocks: List<ClockUiModel> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filteredZones: Map<Char, List<ZoneId>> = emptyMap()
)

data class ClockUiModel(
    val zoneId: ZoneId,
    val cityName: String,
    val timeText: String,
    val dayResId: Int,
    val hoursDiff: Long
)

sealed interface ClockAction {
    data class UpdateSearchQuery(val query: String) : ClockAction
    data class ToggleSearch(val isActive: Boolean) : ClockAction
    data class AddTimeZone(val zoneId: ZoneId) : ClockAction
    data class RemoveTimeZone(val zoneId: ZoneId) : ClockAction
}
