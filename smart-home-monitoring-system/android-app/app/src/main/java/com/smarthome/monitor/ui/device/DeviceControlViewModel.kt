package com.smarthome.monitor.ui.device

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceConfig
import com.smarthome.monitor.data.model.DeviceState
import com.smarthome.monitor.data.model.DeviceType

data class DeviceControlUiState(
    val isLoading: Boolean = true,
    val device: Device? = null
)

class DeviceControlViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState

    fun loadDevice(deviceId: String) {
        val device = when (deviceId) {
            "dev_iron_master" -> Device(
                id = deviceId,
                name = "Smart Steam Iron",
                type = DeviceType.IRON,
                room = "Master Bedroom",
                state = DeviceState(isOn = true, error = true),
                config = DeviceConfig(maxActiveMinutes = 30)
            )
            "dev_cam_entry" -> Device(
                id = deviceId,
                name = "Front Door Cam",
                type = DeviceType.CAMERA,
                room = "Entry",
                state = DeviceState(isOn = true)
            )
            "dev_outlet_coffee" -> Device(
                id = deviceId,
                name = "Living Room Outlet",
                type = DeviceType.OUTLET,
                room = "Kitchen",
                state = DeviceState(isOn = true)
            )
            else -> Device(
                id = deviceId,
                name = "Device",
                type = DeviceType.LIGHT,
                state = DeviceState(isOn = false)
            )
        }
        _uiState.value = DeviceControlUiState(isLoading = false, device = device)
    }

    fun togglePower() {
        val current = _uiState.value.device ?: return
        _uiState.value = _uiState.value.copy(
            device = current.copy(state = current.state.copy(isOn = !current.state.isOn))
        )
    }

    fun refreshSnapshot() {
        // no-op for UI demo
    }
}
