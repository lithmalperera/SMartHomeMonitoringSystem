package com.smarthome.monitor.ui.device

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.*

data class DeviceControlUiState(
    val device: Device? = null,
    val elapsedMinutes: Long = 0
)

class DeviceControlViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState

    fun loadDevice(deviceId: String) {
        val device = mockDevice(deviceId)
        val elapsed = if (device.state.isOn && device.type == DeviceType.IRON) 20L else 0L
        _uiState.value = DeviceControlUiState(device = device, elapsedMinutes = elapsed)
    }

    fun setPower(on: Boolean) {
        val d = uiState.value.device ?: return
        _uiState.value = uiState.value.copy(
            device = d.copy(state = d.state.copy(isOn = on, lastOnAt = if (on) System.currentTimeMillis() else 0L))
        )
    }

    fun setPanelSwitch(key: String, on: Boolean) {
        val d = uiState.value.device ?: return
        _uiState.value = uiState.value.copy(
            device = d.copy(state = d.state.copy(switches = d.state.switches + (key to on)))
        )
    }

    fun setMaxActiveMinutes(minutes: Int) {
        val d = uiState.value.device ?: return
        _uiState.value = uiState.value.copy(
            device = d.copy(config = d.config.copy(maxActiveMinutes = minutes))
        )
    }

    private fun mockDevice(deviceId: String): Device = when {
        deviceId.startsWith("dev_iron") -> Device(
            id = deviceId, name = "Iron", type = DeviceType.IRON, room = "Laundry",
            state = DeviceState(isOn = true, online = true),
            config = DeviceConfig(maxActiveMinutes = 30, wattage = 1100)
        )
        deviceId.startsWith("dev_panel") -> Device(
            id = deviceId, name = "Hall Panel", type = DeviceType.SWITCH_PANEL,
            state = DeviceState(isOn = false, online = true, switches = mapOf("gang1" to false, "gang2" to true))
        )
        deviceId.startsWith("dev_camera") -> Device(
            id = deviceId, name = "Front Camera", type = DeviceType.CAMERA,
            state = DeviceState(online = true, streamUrl = "rtsp://mock/stream", snapshotUrl = "https://picsum.photos/seed/cam/640/360")
        )
        deviceId.startsWith("dev_light") -> Device(
            id = deviceId, name = "Living Room Light", type = DeviceType.LIGHT,
            state = DeviceState(isOn = false, online = true)
        )
        else -> Device(
            id = deviceId, name = "TV Outlet", type = DeviceType.OUTLET,
            state = DeviceState(isOn = true, online = true)
        )
    }
}