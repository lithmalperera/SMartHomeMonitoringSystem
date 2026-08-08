package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    suspend fun turnAllLights(on: Boolean)
    suspend fun turnAllElectricalDevicesOff()
}
