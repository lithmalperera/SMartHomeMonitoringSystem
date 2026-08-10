package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceConfig
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    fun observeDevice(deviceId: String): Flow<Device?>
    suspend fun turnAllLights(on: Boolean)
    suspend fun turnAllElectricalDevicesOff()
    suspend fun addDevice(
        name: String,
        type: DeviceType,
        floorId: String,
        room: String,
        position: GridPosition,
        maxActiveMinutes: Int,
        gangs: Int
    )
    suspend fun deleteDevice(deviceId: String)
    suspend fun setPower(deviceId: String, on: Boolean)
    suspend fun setPanelSwitch(deviceId: String, key: String, on: Boolean)
    suspend fun updateConfig(deviceId: String, config: DeviceConfig)
    suspend fun refreshSnapshot(deviceId: String)
}