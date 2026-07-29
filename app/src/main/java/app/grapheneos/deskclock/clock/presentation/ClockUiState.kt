package app.grapheneos.deskclock.clock.presentation

import java.time.ZoneId

data class TimeUiState(
    val localTime: String = "--:--:--",
    val localDate: String = ""
)

data class ClockUiState(
    val zoneClocks: List<ClockUiModel> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filteredZones: Map<Char, List<ZoneId>> = emptyMap()
)

data class ClockUiModel(
    val zoneId: ZoneId,
    val cityName: String,
    val hours: Int,
    val minutes: Int,
    val dayResId: Int,
    val hoursDiff: Long
)

sealed interface ClockIntent {
    data class UpdateSearchQuery(val query: String) : ClockIntent
    data class ToggleSearch(val isActive: Boolean) : ClockIntent
    data class AddTimeZone(val zoneId: ZoneId) : ClockIntent
    data class RemoveTimeZone(val zoneId: ZoneId) : ClockIntent
}
