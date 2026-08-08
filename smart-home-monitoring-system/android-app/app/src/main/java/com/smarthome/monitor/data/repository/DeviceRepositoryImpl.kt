package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.Device
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

    override suspend fun turnAllLights(on: Boolean) {
        dataSource.turnAllLights(on)
    }

    override suspend fun turnAllElectricalDevicesOff() {
        dataSource.turnAllElectricalDevicesOff()
    }
}
