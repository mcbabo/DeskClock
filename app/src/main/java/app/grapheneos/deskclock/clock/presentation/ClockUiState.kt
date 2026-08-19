package app.grapheneos.deskclock.clock.presentation

import androidx.compose.runtime.Immutable
import java.time.ZoneId

/**
 * Current local time and date for the main clock display.
 */
@Immutable
data class TimeUiState(
    val localTime: String = "--:--:--",
    val localDate: String = ""
)

/**
 * UI state for the World Clock screen.
 */
@Immutable
data class ClockUiState(
    val zoneClocks: List<ClockUiModel> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isEditing: Boolean = false,
    val filteredZones: Map<Char, List<ZoneId>> = emptyMap()
)

/**
 * UI representation of a clock in a specific time zone.
 */
@Immutable
data class ClockUiModel(
    val zoneId: ZoneId,
    val cityName: String,
    val hours: Int,
    val minutes: Int,
    val dayResId: Int,
    val hoursDiff: Long
)

/**
 * User intents for the World Clock screen.
 */
sealed interface ClockIntent {
    data class UpdateSearchQuery(val query: String) : ClockIntent
    data class ToggleSearch(val isActive: Boolean) : ClockIntent
    data class AddTimeZone(val zoneId: ZoneId) : ClockIntent
    data class RemoveTimeZone(val zoneId: ZoneId) : ClockIntent
    data class SetEditing(val isEditing: Boolean) : ClockIntent
}
