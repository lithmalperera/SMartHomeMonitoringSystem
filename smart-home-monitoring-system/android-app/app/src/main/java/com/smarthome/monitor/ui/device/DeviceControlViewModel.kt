package com.smarthome.monitor.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceConfig
import com.smarthome.monitor.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceControlUiState(
    val isLoading: Boolean = true,
    val device: Device? = null
)

@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val deviceId: String = savedStateHandle.get<String>("deviceId") ?: ""

    val uiState: StateFlow<DeviceControlUiState> = deviceRepository.observeDevice(deviceId)
        .map { device ->
            DeviceControlUiState(
                isLoading = false,
                device = device
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviceControlUiState()
        )

    fun togglePower() {
        val device = uiState.value.device ?: return
        viewModelScope.launch {
            deviceRepository.setPower(deviceId, !device.state.isOn)
        }
    }

    fun toggleSwitch(key: String) {
        val device = uiState.value.device ?: return
        val current = device.state.switches[key] ?: false
        viewModelScope.launch {
            deviceRepository.setPanelSwitch(deviceId, key, !current)
        }
    }

    fun setMaxActiveMinutes(minutes: Int) {
        val config = uiState.value.device?.config?.copy(maxActiveMinutes = minutes)
            ?: DeviceConfig(maxActiveMinutes = minutes)
        viewModelScope.launch {
            deviceRepository.updateConfig(deviceId, config)
        }
    }

    fun refreshSnapshot() {
        viewModelScope.launch {
            deviceRepository.refreshSnapshot(deviceId)
        }
    }

    fun deleteDevice(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deviceRepository.deleteDevice(deviceId)
            onDeleted()
        }
    }
}
