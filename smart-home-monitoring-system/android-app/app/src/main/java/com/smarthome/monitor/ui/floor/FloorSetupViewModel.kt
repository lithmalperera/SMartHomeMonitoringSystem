package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.domain.repository.ActivityRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloorSetupViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    fun saveFloor(name: String, rows: Int, cols: Int, imageUrl: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Get the current floors to determine the next order index
                val currentFloors = floorRepository.observeFloors().first()
                val nextOrder = currentFloors.size

                floorRepository.addFloor(name, nextOrder, cols, rows, imageUrl)

                // Log the activity to Firebase RTDB
                activityRepository.logActivity(
                    title = "Added Floor: $name",
                    subtitle = "Android App",
                    type = ActivityType.DOOR
                )

                onSuccess()
            } catch (e: Exception) {
                // Handle/log exception
                e.printStackTrace()
            }
        }
    }
}
