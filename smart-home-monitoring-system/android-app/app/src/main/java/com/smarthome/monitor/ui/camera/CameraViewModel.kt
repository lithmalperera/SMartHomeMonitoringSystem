package com.smarthome.monitor.ui.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceState
import com.smarthome.monitor.data.model.DeviceType

data class CameraUiState(
    val isLoading: Boolean = true,
    val device: Device = Device(),
    val snapshotUrl: String = "",
    val online: Boolean = true,
    val refreshKey: Int = 0
)

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState

    fun load(deviceId: String) {
        _uiState.value = CameraUiState(
            isLoading = false,
            device = Device(
                id = deviceId,
                name = "Front Camera",
                type = DeviceType.CAMERA,
                state = DeviceState(
                    online = true,
                    streamUrl = "rtsp://mock.home.local/stream/$deviceId",
                    snapshotUrl = "https://picsum.photos/seed/$deviceId/640/360"
                )
            ),
            snapshotUrl = "https://picsum.photos/seed/$deviceId/640/360",
            online = true
        )
    }

    fun refreshSnapshot(key: Int) {
        _uiState.value = uiState.value.copy(refreshKey = key)
    }
}