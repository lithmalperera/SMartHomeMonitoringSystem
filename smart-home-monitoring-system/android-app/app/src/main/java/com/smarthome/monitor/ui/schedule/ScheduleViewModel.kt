package com.smarthome.monitor.ui.schedule

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Schedule

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val schedule: Schedule = Schedule(),
    val saved: Boolean = false
)

class ScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState

    fun load(deviceId: String) {
        _uiState.value = ScheduleUiState(
            isLoading = false,
            schedule = Schedule(id = "sched_$deviceId", deviceId = deviceId, onTime = "18:00", offTime = "06:00", enabled = true)
        )
    }

    fun setOnTime(time: String) {
        _uiState.value = uiState.value.copy(schedule = uiState.value.schedule.copy(onTime = time), saved = false)
    }

    fun setOffTime(time: String) {
        _uiState.value = uiState.value.copy(schedule = uiState.value.schedule.copy(offTime = time), saved = false)
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.value = uiState.value.copy(schedule = uiState.value.schedule.copy(enabled = enabled), saved = false)
    }

    fun save() {
        _uiState.value = uiState.value.copy(saved = true)
    }

    fun resetSavedFlag() {
        _uiState.value = uiState.value.copy(saved = false)
    }
}