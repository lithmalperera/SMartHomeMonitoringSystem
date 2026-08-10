package com.smarthome.monitor.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
import com.smarthome.monitor.domain.repository.ActivityRepository
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GridInfo(
    val gridColumns: Int = 8,
    val gridRows: Int = 8
)

@HiltViewModel
class DevicePlacementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val activityRepository: ActivityRepository,
    private val floorRepository: FloorRepository
) : ViewModel() {

    val floorId: String = savedStateHandle.get<String>("floorId") ?: "floor_ground"

    private val _grid = MutableStateFlow(GridInfo())
    val grid: StateFlow<GridInfo> = _grid

    init {
        viewModelScope.launch {
            try {
                val floor = floorRepository.observeFloors().first().firstOrNull { it.id == floorId }
                if (floor != null) {
                    _grid.value = GridInfo(
                        gridColumns = floor.gridColumns,
                        gridRows = floor.gridRows
                    )
                }
            } catch (e: Exception) {
                // Keep default 8x8 grid when the floor cannot be loaded
            }
        }
    }

    fun saveDevice(
        name: String,
        type: DeviceType,
        cellIndex: Int,
        maxActiveMinutes: Int,
        gangs: Int,
        onSuccess: () -> Unit
    ) {
        val cols = _grid.value.gridColumns.coerceAtLeast(1)
        viewModelScope.launch {
            try {
                deviceRepository.addDevice(
                    name = name,
                    type = type,
                    floorId = floorId,
                    room = "General",
                    position = GridPosition(x = cellIndex % cols, y = cellIndex / cols),
                    maxActiveMinutes = maxActiveMinutes,
                    gangs = gangs
                )

                activityRepository.logActivity(
                    title = "Added Device: $name",
                    subtitle = "Android App",
                    type = ActivityType.DOOR
                )

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
