package app.grapheneos.deskclock.clock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.data.ClockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _allAvailableZones = MutableStateFlow<List<ZoneId>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val zones = ZoneId.getAvailableZoneIds()
                .map { ZoneId.of(it) }
                .sortedBy { it.id.substringAfter('/') }
            _allAvailableZones.value = zones
        }
    }

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

    private val filteredZonesFlow = combine(_searchQuery, _allAvailableZones) { query, allZones ->
        val filtered = if (query.isBlank()) {
            allZones
        } else {
            allZones.filter {
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
        filteredZonesFlow,
        timeUiState
    ) { selectedZones, query, isSearch, filtered, _ ->
        val now = ZonedDateTime.now()
        ClockUiState(
            zoneClocks = selectedZones.map { formatToClockUiModel(now, it.zoneId) },
            searchQuery = query,
            isSearchActive = isSearch,
            filteredZones = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockUiState())

    fun handleIntent(intent: ClockIntent) {
        when (intent) {
            is ClockIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is ClockIntent.ToggleSearch -> _isSearchActive.value = intent.isActive
            is ClockIntent.AddTimeZone -> viewModelScope.launch {
                repository.addZone(intent.zoneId)
                handleIntent(ClockIntent.ToggleSearch(false))
            }

            is ClockIntent.RemoveTimeZone -> viewModelScope.launch {
                repository.removeZone(intent.zoneId)
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
            hours = targetTime.hour,
            minutes = targetTime.minute,
            dayResId = dayResId,
            hoursDiff = (targetTime.offset.totalSeconds - now.offset.totalSeconds) / 3600L
        )
    }
}
