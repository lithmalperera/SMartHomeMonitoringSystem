package com.smarthome.monitor.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.*

data class FloorSummary(
    val floor: Floor,
    val deviceCount: Int,
    val onCount: Int
)

data class SafetyAlert(
    val deviceId: String,
    val deviceName: String,
    val onMinutes: Long
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val floors: List<FloorSummary> = emptyList(),
    val alerts: List<SafetyAlert> = emptyList(),
    val showAddFloorDialog: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        _uiState.value = HomeUiState(
            isLoading = false,
            floors = listOf(
                FloorSummary(Floor(id = "floor_ground", name = "Ground Floor", order = 0), deviceCount = 4, onCount = 2),
                FloorSummary(Floor(id = "floor_first", name = "First Floor", order = 1), deviceCount = 3, onCount = 0)
            ),
            alerts = listOf(
                SafetyAlert(deviceId = "dev_iron_living", deviceName = "Living Room Iron", onMinutes = 20)
            )
        )
    }

    fun showAddFloorDialog(show: Boolean) {
        _uiState.value = uiState.value.copy(showAddFloorDialog = show)
    }

    fun addFloor(name: String) {
        if (name.isBlank()) return
        val newFloor = Floor(
            id = "floor_${name.lowercase().replace(" ", "_")}",
            name = name,
            order = uiState.value.floors.size
        )
        _uiState.value = uiState.value.copy(
            floors = uiState.value.floors + FloorSummary(newFloor, 0, 0),
            showAddFloorDialog = false
        )
    }
}