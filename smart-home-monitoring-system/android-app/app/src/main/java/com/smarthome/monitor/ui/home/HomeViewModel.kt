package com.smarthome.monitor.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Floor

data class HomeUiState(
    val isLoading: Boolean = true,
    val floors: List<Floor> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        _uiState.value = HomeUiState(
            isLoading = false,
            floors = listOf(
                Floor(id = "floor_ground", name = "Ground Floor", order = 0),
                Floor(id = "floor_first", name = "First Floor", order = 1)
            )
        )
    }
}
