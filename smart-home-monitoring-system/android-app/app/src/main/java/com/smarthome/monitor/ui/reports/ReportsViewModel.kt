package com.smarthome.monitor.ui.reports

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UsageHistoryItem(
    val name: String,
    val period: String,
    val duration: String,
    val energy: String,
    val tag: String,
    val icon: ImageVector
)

data class ReportsUiState(
    val isLoading: Boolean = false,
    val text: String = "Reports coming soon",
    val history: List<UsageHistoryItem> = emptyList()
)

class ReportsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    init {
        _uiState.value = ReportsUiState(
            isLoading = false,
            history = listOf(
                UsageHistoryItem(
                    name = "Living Room AC",
                    period = "Today, 2:30 PM - 4:45 PM",
                    duration = "2h 15m",
                    energy = "3.2 kWh",
                    tag = "Standard",
                    icon = Icons.Default.AcUnit
                ),
                UsageHistoryItem(
                    name = "EV Charger",
                    period = "Yesterday, 11:00 PM - 3:00 AM",
                    duration = "4h 00m",
                    energy = "22.0 kWh",
                    tag = "Peak Hours",
                    icon = Icons.Default.EvStation
                ),
                UsageHistoryItem(
                    name = "Kitchen Lights",
                    period = "Yesterday, 6:00 AM - 6:45 PM",
                    duration = "12h 45m",
                    energy = "0.8 kWh",
                    tag = "Efficient",
                    icon = Icons.Default.Lightbulb
                )
            )
        )
    }
}
