package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.Alert
import com.smarthome.monitor.data.model.AlertSeverity
import com.smarthome.monitor.data.remote.FirebaseAlertDataSource
import com.smarthome.monitor.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseAlertDataSource
) : AlertRepository {

    override fun observeAlerts(): Flow<List<Alert>> = dataSource.observeAlerts()

    override suspend fun markRead(alertId: String) {
        dataSource.markRead(alertId)
    }

    override suspend fun markAllRead() {
        dataSource.markAllRead()
    }

    override suspend fun addAlert(
        deviceId: String,
        title: String,
        message: String,
        severity: AlertSeverity
    ) {
        dataSource.addAlert(deviceId, title, message, severity)
    }
}
