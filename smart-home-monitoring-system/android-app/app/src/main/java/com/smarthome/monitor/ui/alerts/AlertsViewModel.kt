package com.smarthome.monitor.ui.alerts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AlertSeverity { CRITICAL, SECURITY, INFO, ROUTINE }

data class AlertItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val severity: AlertSeverity,
    val isRead: Boolean = false
)

data class AlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<AlertItem> = emptyList(),
    val newAlerts: Int = 0,
    val alertsCount: Int = 0
)

class AlertsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState

    init {
        _uiState.value = AlertsUiState(
            isLoading = false,
            alerts = listOf(
                AlertItem(
                    id = "alert_iron",
                    title = "Master Iron",
                    message = "Max duration exceeded. Auto-shutoff initiated for safety.",
                    time = "2m ago",
                    severity = AlertSeverity.CRITICAL
                ),
                AlertItem(
                    id = "alert_camera",
                    title = "Front Door Camera",
                    message = "Motion detected at entrance. Person identified as \"Courier\".",
                    time = "15m ago",
                    severity = AlertSeverity.SECURITY
                ),
                AlertItem(
                    id = "alert_light",
                    title = "Living Room Lamp",
                    message = "Scheduled ON. Welcome scene activated.",
                    time = "1h ago",
                    severity = AlertSeverity.INFO
                ),
                AlertItem(
                    id = "alert_routine",
                    title = "Night Routine",
                    message = "All doors locked. Security system armed to Stay mode.",
                    time = "Yesterday",
                    severity = AlertSeverity.ROUTINE
                )
            ),
            newAlerts = 3,
            alertsCount = 2
        )
    }
}
