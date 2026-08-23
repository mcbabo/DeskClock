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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class ClockViewModel(private val clockRepository: ClockRepository) : ViewModel() {

    private data class InternalState(
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val isEditing: Boolean = false
    )

    private val internalState = MutableStateFlow(InternalState())

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

    private val allAvailableZones = flow {
        val zones = ZoneId.getAvailableZoneIds()
            .asSequence()
            .map { ZoneId.of(it) }
            .sortedBy { it.id.substringAfter('/') }
            .toList()
        emit(zones)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val filteredZones = combine(internalState, allAvailableZones) { internal, allZones ->
        if (internal.searchQuery.isBlank()) {
            allZones
        } else {
            allZones.filter {
                it.id.contains(internal.searchQuery, ignoreCase = true) ||
                    it.id.replace('_', ' ').contains(internal.searchQuery, ignoreCase = true)
            }
        }.groupBy { it.id.substringAfter('/').first().uppercaseChar() }
            .toSortedMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<ClockUiState> = combine(
        clockRepository.getSelectedClocks(),
        internalState,
        filteredZones,
        flow {
            while (true) {
                emit(Unit)
                // World clocks only need minute precision
                delay(60.seconds)
            }
        }
    ) { selectedZones, internal, filtered, _ ->
        val now = ZonedDateTime.now()
        val zoneClocks = selectedZones.map { formatToClockUiModel(now, it.zoneId) }

        ClockUiState(
            zoneClocks = zoneClocks,
            filteredZones = filtered,
            searchQuery = internal.searchQuery,
            isSearchActive = internal.isSearchActive,
            isEditing = internal.isEditing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockUiState())

    fun handleIntent(intent: ClockIntent) {
        when (intent) {
            is ClockIntent.UpdateSearchQuery -> internalState.update { it.copy(searchQuery = intent.query) }
            is ClockIntent.ToggleSearch -> internalState.update { it.copy(isSearchActive = intent.isActive) }
            is ClockIntent.AddTimeZone -> viewModelScope.launch {
                clockRepository.addZone(intent.zoneId)
                handleIntent(ClockIntent.ToggleSearch(false))
            }

            is ClockIntent.RemoveTimeZone -> viewModelScope.launch {
                clockRepository.removeZone(intent.zoneId)
            }

            is ClockIntent.SetEditing -> internalState.update { it.copy(isEditing = intent.isEditing) }
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
