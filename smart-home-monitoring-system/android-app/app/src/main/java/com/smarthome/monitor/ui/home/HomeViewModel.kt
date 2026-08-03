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

data class RecentActivity(
    val id: String,
    val title: String,
    val time: String,
    val subtitle: String,
    val type: ActivityType
)

enum class ActivityType { DOOR, CLIMATE, LOCK, MOTION }

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "Videesha",
    val date: String = "Sunday, 03 August 2026",
    val time: String = "06:45 PM",
    val lastSync: String = "2 seconds ago",
    val floorsCount: Int = 3,
    val totalDevices: Int = 24,
    val onlineCount: Int = 21,
    val alertsCount: Int = 2,
    val properties: List<PropertyItem> = emptyList(),
    val floors: List<Floor> = emptyList(),
    val selectedFloor: Floor? = null,
    val onCount: Int = 0,
    val errorCount: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        _uiState.value = HomeUiState(
            isLoading = false,
            userName = "Videesha",
            date = "Sunday, 03 August 2026",
            time = "06:45 PM",
            lastSync = "2 seconds ago",
            floorsCount = 3,
            totalDevices = 24,
            onlineCount = 21,
            alertsCount = 2,
            properties = listOf(
                PropertyItem(id = "prop_main", name = "Main Residence", location = "Palo Alto, CA", isPrimary = true)
            ),
            floors = listOf(
                Floor(id = "floor_ground", name = "Ground Floor", order = 0),
                Floor(id = "floor_first", name = "First Floor", order = 1),
                Floor(id = "floor_garage", name = "Garage", order = 2)
            ),
            recentActivities = listOf(
                RecentActivity("1", "Garage Door Closed", "10:15 AM", "Auto", ActivityType.DOOR),
                RecentActivity("2", "Climate Adjusted", "09:30 AM", "Smart Schedule", ActivityType.CLIMATE),
                RecentActivity("3", "Front Door Locked", "08:45 AM", "Alex", ActivityType.LOCK),
                RecentActivity("4", "Motion Detected", "04:12 AM", "Backyard", ActivityType.MOTION)
            )
        )
    }
}
