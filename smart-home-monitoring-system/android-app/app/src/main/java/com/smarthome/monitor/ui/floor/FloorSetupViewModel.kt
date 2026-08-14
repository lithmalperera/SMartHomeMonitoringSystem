package com.smarthome.monitor.ui.floor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.domain.repository.ActivityRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloorSetupViewModel @Inject constructor(
    private val floorRepository: FloorRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _pickedImagePath = MutableStateFlow<String?>(null)
    val pickedImagePath: StateFlow<String?> = _pickedImagePath

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onImagePicked(contentUri: String) {
        viewModelScope.launch {
            _pickedImagePath.value = floorRepository.cacheFloorImage(contentUri)
        }
    }

    fun saveFloor(name: String, rows: Int, cols: Int, onSuccess: () -> Unit) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                val remoteImageUrl = _pickedImagePath.value?.let { path ->
                    floorRepository.uploadFloorImage(path)
                }

                // Get the current floors to determine the next order index
                val currentFloors = floorRepository.observeFloors().first()
                val nextOrder = currentFloors.size

                floorRepository.addFloor(name, nextOrder, cols, rows, remoteImageUrl)

                // Log the activity to Firebase RTDB
                activityRepository.logActivity(
                    title = "Added Floor: $name",
                    subtitle = "Android App",
                    type = ActivityType.DOOR
                )

                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = when {
                    e.message?.contains("Object does not exist", ignoreCase = true) == true ->
                        "The selected image is no longer accessible. Please pick it again and save."
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "Storage permission denied. Check the Firebase Storage security rules."
                    else -> e.message ?: "Failed to save floor"
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
