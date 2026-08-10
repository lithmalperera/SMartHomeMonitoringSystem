package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceConfig
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
import com.smarthome.monitor.data.remote.FirebaseDeviceDataSource
import com.smarthome.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseDeviceDataSource
) : DeviceRepository {

    override fun observeDevices(): Flow<List<Device>> = dataSource.observeDevices()

    override fun observeDevice(deviceId: String): Flow<Device?> = dataSource.observeDevice(deviceId)

    override suspend fun turnAllLights(on: Boolean) {
        dataSource.turnAllLights(on)
    }

    override suspend fun turnAllElectricalDevicesOff() {
        dataSource.turnAllElectricalDevicesOff()
    }

    override suspend fun addDevice(
        name: String,
        type: DeviceType,
        floorId: String,
        room: String,
        position: GridPosition,
        maxActiveMinutes: Int,
        gangs: Int
    ) {
        dataSource.addDevice(name, type, floorId, room, position, maxActiveMinutes, gangs)
    }

    override suspend fun deleteDevice(deviceId: String) {
        dataSource.deleteDevice(deviceId)
    }

    override suspend fun setPower(deviceId: String, on: Boolean) {
        dataSource.setPower(deviceId, on)
    }

    override suspend fun setPanelSwitch(deviceId: String, key: String, on: Boolean) {
        dataSource.setPanelSwitch(deviceId, key, on)
    }

    override suspend fun updateConfig(deviceId: String, config: DeviceConfig) {
        dataSource.updateConfig(deviceId, config)
    }

    override suspend fun refreshSnapshot(deviceId: String) {
        dataSource.refreshSnapshot(deviceId)
    }
}