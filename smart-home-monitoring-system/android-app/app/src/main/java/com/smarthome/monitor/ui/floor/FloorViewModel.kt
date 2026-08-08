package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FloorUiState(
    val isLoading: Boolean = true,
    val floorName: String = "",
    val imageUrl: String? = null,
    val devices: List<Device> = emptyList(),
    val alertsCount: Int = 0
)

@HiltViewModel
class FloorViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FloorUiState())
    val uiState: StateFlow<FloorUiState> = _uiState

    fun loadFloor(floorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                floorRepository.observeFloors(),
                deviceRepository.observeDevices()
            ) { floors, devices ->
                val floor = floors.find { it.id == floorId }
                val floorDevices = devices.filter { it.floorId == floorId }

                if (floor != null) {
                    FloorUiState(
                        isLoading = false,
                        floorName = floor.name,
                        imageUrl = floor.imageUrl,
                        devices = floorDevices,
                        alertsCount = floorDevices.count { it.state.error }
                    )
                } else {
                    val mockName = when (floorId) {
                        "floor_ground" -> "Ground Floor"
                        "floor_first" -> "1st Floor"
                        "floor_basement" -> "Basement"
                        else -> "Floor"
                    }
                    FloorUiState(
                        isLoading = false,
                        floorName = mockName,
                        devices = emptyList(),
                        alertsCount = 0
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
