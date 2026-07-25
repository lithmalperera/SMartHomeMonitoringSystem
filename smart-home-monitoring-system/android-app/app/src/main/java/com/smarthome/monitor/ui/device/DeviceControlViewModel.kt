package com.smarthome.monitor.ui.device

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.*

data class DeviceControlUiState(
    val device: Device? = null
)

class DeviceControlViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState

    fun loadDevice(deviceId: String) {
        _uiState.value = DeviceControlUiState(
            device = Device(id = deviceId, name = "Device", type = DeviceType.LIGHT)
        )
    }
}