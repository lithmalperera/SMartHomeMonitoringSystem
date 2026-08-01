package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceState
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition

data class FloorUiState(
    val isLoading: Boolean = true,
    val floorName: String = "",
    val devices: List<Device> = emptyList(),
    val alertsCount: Int = 0
)

class FloorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FloorUiState())
    val uiState: StateFlow<FloorUiState> = _uiState

    fun loadFloor(floorId: String) {
        val floorName = when (floorId) {
            "floor_ground" -> "Ground Floor"
            "floor_first" -> "1st Floor"
            "floor_basement" -> "Basement"
            "floor_outdoor" -> "Outdoor"
            else -> "Floor"
        }
        _uiState.value = FloorUiState(
            isLoading = false,
            floorName = floorName,
            devices = listOf(
                Device(
                    id = "dev_light_living",
                    name = "Living Room",
                    type = DeviceType.LIGHT,
                    floorId = floorId,
                    room = "Living Room",
                    position = GridPosition(3, 2),
                    state = DeviceState(isOn = true)
                ),
                Device(
                    id = "dev_outlet_coffee",
                    name = "Coffee Maker",
                    type = DeviceType.OUTLET,
                    floorId = floorId,
                    room = "Kitchen",
                    position = GridPosition(1, 1),
                    state = DeviceState(isOn = false)
                ),
                Device(
                    id = "dev_iron_master",
                    name = "Master Iron",
                    type = DeviceType.IRON,
                    floorId = floorId,
                    room = "Master Bedroom",
                    position = GridPosition(6, 4),
                    state = DeviceState(isOn = true, error = true)
                ),
                Device(
                    id = "dev_outlet_air",
                    name = "Air Purifier",
                    type = DeviceType.OUTLET,
                    floorId = floorId,
                    room = "Hallway",
                    position = GridPosition(2, 4),
                    state = DeviceState(isOn = true)
                ),
                Device(
                    id = "dev_cam_entry",
                    name = "Entry Cam",
                    type = DeviceType.CAMERA,
                    floorId = floorId,
                    room = "Entry",
                    position = GridPosition(0, 6),
                    state = DeviceState(isOn = true)
                )
            ),
            alertsCount = 2
        )
    }
}
