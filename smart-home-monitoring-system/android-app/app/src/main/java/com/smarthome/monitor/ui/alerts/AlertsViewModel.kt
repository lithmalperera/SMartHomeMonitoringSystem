package com.smarthome.monitor.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Alert
import com.smarthome.monitor.domain.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<Alert> = emptyList(),
    val newAlerts: Int = 0,
    val alertsCount: Int = 0
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {

    val uiState: StateFlow<AlertsUiState> = alertRepository.observeAlerts()
        .map { alerts ->
            val unread = alerts.count { !it.isRead }
            AlertsUiState(
                isLoading = false,
                alerts = alerts,
                newAlerts = unread,
                alertsCount = unread
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlertsUiState()
        )

    fun markRead(alertId: String) {
        viewModelScope.launch {
            alertRepository.markRead(alertId)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            alertRepository.markAllRead()
        }
    }
}
