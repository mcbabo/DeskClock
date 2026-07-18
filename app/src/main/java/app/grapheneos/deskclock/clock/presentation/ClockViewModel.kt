package app.grapheneos.deskclock.clock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.data.ClockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)

    val timeUiState: StateFlow<TimeUiState> = flow {
        while (true) {
            val now = ZonedDateTime.now()
            emit(
                TimeUiState(
                    localTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    localDate = now.format(DateTimeFormatter.ofPattern("EEE, d. MMM"))
                )
            )
            delay(1.seconds)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeUiState())

    private val allAvailableZones = ZoneId.getAvailableZoneIds()
        .map { ZoneId.of(it) }
        .sortedBy { it.id.substringAfter('/') }

    private val filteredZonesFlow = _searchQuery
        .map { query ->
            val filtered = if (query.isBlank()) {
                allAvailableZones
            } else {
                allAvailableZones.filter {
                    it.id.contains(query, ignoreCase = true) ||
                        it.id.replace('_', ' ').contains(query, ignoreCase = true)
                }
            }

            filtered.groupBy { it.id.substringAfter('/').first().uppercaseChar() }
                .toSortedMap()
        }

    val uiState: StateFlow<ClockUiState> = combine(
        repository.getSelectedClocks(),
        _searchQuery,
        _isSearchActive,
        filteredZonesFlow
    ) { selectedZones, query, isSearch, filtered ->
        val now = ZonedDateTime.now()
        ClockUiState(
            zoneClocks = selectedZones.map { formatToClockUiModel(now, it.zoneId) },
            searchQuery = query,
            isSearchActive = isSearch,
            filteredZones = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockUiState())

    fun handleAction(action: ClockAction) {
        when (action) {
            is ClockAction.UpdateSearchQuery -> _searchQuery.value = action.query
            is ClockAction.ToggleSearch -> _isSearchActive.value = action.isActive
            is ClockAction.AddTimeZone -> viewModelScope.launch {
                repository.addZone(action.zoneId)
                handleAction(ClockAction.ToggleSearch(false))
            }

            is ClockAction.RemoveTimeZone -> viewModelScope.launch {
                repository.removeZone(action.zoneId)
            }
        }
    }

    private fun formatToClockUiModel(now: ZonedDateTime, targetZone: ZoneId): ClockUiModel {
        val targetTime = now.withZoneSameInstant(targetZone)
        val localDate = now.toLocalDate()
        val targetDate = targetTime.toLocalDate()

        val dayResId = when {
            targetDate.isAfter(localDate) -> R.string.tomorrow
            targetDate.isBefore(localDate) -> R.string.yesterday
            else -> R.string.today
        }

        return ClockUiModel(
            zoneId = targetZone,
            cityName = targetZone.id.substringAfter('/').replace('_', ' '),
            timeText = targetTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            dayResId = dayResId,
            hoursDiff = (targetTime.offset.totalSeconds - now.offset.totalSeconds) / 3600L
        )
    }
}
