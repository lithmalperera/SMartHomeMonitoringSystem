package com.smarthome.monitor.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.data.model.Alert
import com.smarthome.monitor.domain.repository.AlertRepository
import com.smarthome.monitor.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<Alert> = emptyList(),
    val deviceNames: Map<String, String> = emptyMap(),
    val newAlerts: Int = 0,
    val alertsCount: Int = 0
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    deviceRepository: DeviceRepository
) : ViewModel() {

    val uiState: StateFlow<AlertsUiState> = combine(
        alertRepository.observeAlerts(),
        deviceRepository.observeDevices()
    ) { alerts, devices ->
        val unread = alerts.count { !it.read }
        AlertsUiState(
            isLoading = false,
            alerts = alerts,
            deviceNames = devices.associate { it.id to it.name },
            newAlerts = unread,
            alertsCount = unread
        )
    }.stateIn(
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
