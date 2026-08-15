package com.smarthome.monitor.ui.floor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FloorUiState(
    val isLoading: Boolean = true,
    val floorName: String = "",
    val floors: List<Floor> = emptyList(),
    val imageUrl: Any? = null,
    val devices: List<Device> = emptyList(),
    val gridColumns: Int = 4,
    val gridRows: Int = 4,
    val alertsCount: Int = 0
)

@HiltViewModel
class FloorViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context
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
                        floors = floors,
                        imageUrl = resolveFloorImage(floorId),
                        devices = floorDevices,
                        gridColumns = floor.gridColumns,
                        gridRows = floor.gridRows,
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
                        floors = floors,
                        devices = emptyList(),
                        alertsCount = 0
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun resolveFloorImage(floorId: String): Any? {
        val dir = File(context.filesDir, "floor_plans")
        val file = dir.listFiles()?.firstOrNull { it.nameWithoutExtension == floorId }
            ?.takeIf { it.exists() }
        android.util.Log.d("FloorVM", "resolve floorId=$floorId found=$file")
        return file
    }
}
