package com.smarthome.monitor.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Floor

data class PropertyItem(
    val id: String,
    val name: String,
    val location: String,
    val isPrimary: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val properties: List<PropertyItem> = emptyList(),
    val floors: List<Floor> = emptyList(),
    val selectedFloor: Floor? = null,
    val onCount: Int = 0,
    val alertsCount: Int = 0,
    val errorCount: Int = 0
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        _uiState.value = HomeUiState(
            isLoading = false,
            properties = listOf(
                PropertyItem(id = "prop_main", name = "Main Residence", location = "Palo Alto, CA", isPrimary = true),
                PropertyItem(id = "prop_summer", name = "Summer House", location = "Malibu, CA", isPrimary = false)
            ),
            floors = listOf(
                Floor(id = "floor_ground", name = "Ground Floor", order = 0, gridColumns = 8, gridRows = 8),
                Floor(id = "floor_first", name = "1st Floor", order = 1, gridColumns = 8, gridRows = 8),
                Floor(id = "floor_basement", name = "Basement", order = 2, gridColumns = 6, gridRows = 6),
                Floor(id = "floor_outdoor", name = "Outdoor", order = 3, gridColumns = 6, gridRows = 6)
            ),
            selectedFloor = Floor(id = "floor_ground", name = "Ground Floor", order = 0, gridColumns = 8, gridRows = 8),
            onCount = 12,
            alertsCount = 2,
            errorCount = 1
        )
    }
}
