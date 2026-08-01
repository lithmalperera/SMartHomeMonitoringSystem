package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.*

data class FloorUiState(
    val isLoading: Boolean = true,
    val floor: Floor = Floor(),
    val devices: List<Device> = emptyList(),
    val showAddDeviceDialog: Boolean = false
)

class FloorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FloorUiState())
    val uiState: StateFlow<FloorUiState> = _uiState

    fun loadFloor(floorId: String) {
        _uiState.value = FloorUiState(
            isLoading = false,
            floor = if (floorId == "floor_ground")
                Floor(id = "floor_ground", name = "Ground Floor", order = 0, gridColumns = 3, gridRows = 3)
            else
                Floor(id = "floor_first", name = "First Floor", order = 1, gridColumns = 3, gridRows = 3),
            devices = listOf(
                Device(id = "dev_light_living", name = "Living Room Light", type = DeviceType.LIGHT, floorId = floorId, room = "Living Room", position = GridPosition(0, 0), state = DeviceState(isOn = false, online = true)),
                Device(id = "dev_outlet_tv", name = "TV Outlet", type = DeviceType.OUTLET, floorId = floorId, room = "Living Room", position = GridPosition(1, 0), state = DeviceState(isOn = true, online = true)),
                Device(id = "dev_iron_living", name = "Iron", type = DeviceType.IRON, floorId = floorId, room = "Laundry", position = GridPosition(2, 1), state = DeviceState(isOn = true, online = true)),
                Device(id = "dev_panel_hall", name = "Hall Panel", type = DeviceType.SWITCH_PANEL, floorId = floorId, room = "Hall", position = GridPosition(0, 2), state = DeviceState(isOn = false, online = true, switches = mapOf("gang1" to false, "gang2" to true)))
            )
        )
    }

    fun showAddDeviceDialog(show: Boolean) {
        _uiState.value = uiState.value.copy(showAddDeviceDialog = show)
    }

    fun addDevice(name: String, type: DeviceType, room: String, x: Int, y: Int) {
        if (name.isBlank()) return
        val floorId = uiState.value.floor.id
        val device = Device(
            id = "dev_${name.lowercase().replace(" ", "_")}",
            name = name,
            type = type,
            floorId = floorId,
            room = room,
            position = GridPosition(x, y),
            state = DeviceState()
        )
        _uiState.value = uiState.value.copy(
            devices = uiState.value.devices + device,
            showAddDeviceDialog = false
        )
    }

    fun deleteDevice(deviceId: String) {
        _uiState.value = uiState.value.copy(devices = uiState.value.devices.filterNot { it.id == deviceId })
    }
}