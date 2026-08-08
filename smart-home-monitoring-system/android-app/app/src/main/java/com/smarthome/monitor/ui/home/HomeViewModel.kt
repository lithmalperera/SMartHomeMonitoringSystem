package com.smarthome.monitor.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.domain.repository.ActivityRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.smarthome.monitor.data.model.ActivityType as ModelActivityType
import com.smarthome.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.launch

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
    val lastSync: String = "Just now",
    val floorsCount: Int = 0,
    val totalDevices: Int = 0,
    val onlineCount: Int = 0,
    val alertsCount: Int = 0,
    val properties: List<PropertyItem> = emptyList(),
    val floors: List<Floor> = emptyList(),
    val selectedFloor: Floor? = null,
    val onCount: Int = 0,
    val errorCount: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val activityRepository: ActivityRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    fun turnAllLights(on: Boolean) {
        viewModelScope.launch {
            try {
                deviceRepository.turnAllLights(on)
                activityRepository.logActivity(
                    title = "All Lights turned ${if (on) "ON" else "OFF"}",
                    subtitle = "Android App",
                    type = ModelActivityType.DOOR
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun turnAllElectricalDevicesOff() {
        viewModelScope.launch {
            try {
                deviceRepository.turnAllElectricalDevicesOff()
                activityRepository.logActivity(
                    title = "All Electrical Devices turned OFF",
                    subtitle = "Android App",
                    type = ModelActivityType.DOOR
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun emergencyOff() {
        viewModelScope.launch {
            try {
                deviceRepository.turnAllLights(false)
                deviceRepository.turnAllElectricalDevicesOff()
                activityRepository.logActivity(
                    title = "EMERGENCY OFF TRIGGERED",
                    subtitle = "All devices and lights turned off",
                    type = ModelActivityType.LOCK
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _properties = listOf(
        PropertyItem(id = "prop_main", name = "Main Residence", location = "Palo Alto, CA", isPrimary = true)
    )

    val uiState: StateFlow<HomeUiState> = combine(
        floorRepository.observeFloors(),
        activityRepository.observeActivities()
    ) { floorsList, activitiesList ->
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        HomeUiState(
            isLoading = false,
            userName = "Videesha",
            date = currentDate,
            time = currentTime,
            lastSync = "Just now",
            floorsCount = floorsList.size,
            totalDevices = floorsList.size * 4,
            onlineCount = floorsList.size * 3,
            alertsCount = if (floorsList.isNotEmpty()) 1 else 0,
            properties = _properties,
            floors = floorsList,
            recentActivities = activitiesList.map { dbActivity ->
                RecentActivity(
                    id = dbActivity.id,
                    title = dbActivity.title,
                    time = formatTimestampToTimeAgo(dbActivity.timestamp),
                    subtitle = dbActivity.subtitle,
                    type = mapActivityType(dbActivity.type)
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun mapActivityType(type: com.smarthome.monitor.data.model.ActivityType): ActivityType {
        return when (type) {
            com.smarthome.monitor.data.model.ActivityType.DOOR -> ActivityType.DOOR
            com.smarthome.monitor.data.model.ActivityType.CLIMATE -> ActivityType.CLIMATE
            com.smarthome.monitor.data.model.ActivityType.LOCK -> ActivityType.LOCK
            com.smarthome.monitor.data.model.ActivityType.MOTION -> ActivityType.MOTION
        }
    }

    private fun formatTimestampToTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val difference = now - timestamp
        return when {
            difference < 60000 -> "Just now"
            difference < 3600000 -> "${difference / 60000}m ago"
            difference < 86400000 -> {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
