package com.smarthome.monitor.ui.schedule

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val text: String = "Schedule coming soon"
)

class ScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState
}
