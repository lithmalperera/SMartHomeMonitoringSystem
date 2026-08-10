package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlerts(): Flow<List<Alert>>
    suspend fun markRead(alertId: String)
    suspend fun markAllRead()
}
