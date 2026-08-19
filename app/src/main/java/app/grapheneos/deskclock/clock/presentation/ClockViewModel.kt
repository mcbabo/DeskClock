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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class ClockViewModel(private val clockRepository: ClockRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private val _allAvailableZones = MutableStateFlow<List<ZoneId>>(emptyList())

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

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val zones = ZoneId.getAvailableZoneIds()
                .map { ZoneId.of(it) }
                .sortedBy { it.id.substringAfter('/') }
            _allAvailableZones.value = zones
        }

        observeSelectedClocks()
        observeFilteredZones()
    }

    private fun observeSelectedClocks() {
        combine(
            clockRepository.getSelectedClocks(),
            timeUiState
        ) { selectedZones, _ ->
            val now = ZonedDateTime.now()
            selectedZones.map { formatToClockUiModel(now, it.zoneId) }
        }.onEach { uiModels ->
            _uiState.update { it.copy(zoneClocks = uiModels) }
        }.launchIn(viewModelScope)
    }

    private fun observeFilteredZones() {
        combine(
            _uiState.map { it.searchQuery }.distinctUntilChanged(),
            _allAvailableZones
        ) { query, allZones ->
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
        }.onEach { filtered ->
            _uiState.update { it.copy(filteredZones = filtered) }
        }.launchIn(viewModelScope)
    }

    fun handleIntent(intent: ClockIntent) {
        when (intent) {
            is ClockIntent.UpdateSearchQuery -> _uiState.update { it.copy(searchQuery = intent.query) }
            is ClockIntent.ToggleSearch -> _uiState.update { it.copy(isSearchActive = intent.isActive) }
            is ClockIntent.AddTimeZone -> viewModelScope.launch {
                clockRepository.addZone(intent.zoneId)
                handleIntent(ClockIntent.ToggleSearch(false))
            }

            is ClockIntent.RemoveTimeZone -> viewModelScope.launch {
                clockRepository.removeZone(intent.zoneId)
            }

            is ClockIntent.SetEditing -> _uiState.update { it.copy(isEditing = intent.isEditing) }
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
