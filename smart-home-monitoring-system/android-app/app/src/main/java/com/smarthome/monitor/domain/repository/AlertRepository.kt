package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Alert
import com.smarthome.monitor.data.model.AlertSeverity
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlerts(): Flow<List<Alert>>
    suspend fun markRead(alertId: String)
    suspend fun markAllRead()
    suspend fun addAlert(
        deviceId: String,
        title: String,
        message: String,
        severity: AlertSeverity
    )
}
