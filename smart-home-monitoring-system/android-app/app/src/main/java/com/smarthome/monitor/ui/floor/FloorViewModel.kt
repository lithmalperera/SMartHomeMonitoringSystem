package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.*

data class FloorUiState(
    val isLoading: Boolean = true,
    val floorName: String = "",
    val devices: List<Device> = emptyList()
)

class FloorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FloorUiState())
    val uiState: StateFlow<FloorUiState> = _uiState

    fun loadFloor(floorId: String) {
        _uiState.value = FloorUiState(
            isLoading = false,
            floorName = if (floorId == "floor_ground") "Ground Floor" else "First Floor",
            devices = listOf(
                Device(id = "dev_light_living", name = "Living Room Light", type = DeviceType.LIGHT, floorId = floorId, room = "Living Room", position = GridPosition(0, 0), state = DeviceState(isOn = false, online = true)),
                Device(id = "dev_outlet_tv", name = "TV Outlet", type = DeviceType.OUTLET, floorId = floorId, room = "Living Room", position = GridPosition(1, 0), state = DeviceState(isOn = true, online = true))
            )
        )
    }
}
