package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.data.remote.FirebaseFloorDataSource
import com.smarthome.monitor.domain.repository.FloorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloorRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFloorDataSource
) : FloorRepository {
    override fun observeFloors(): Flow<List<Floor>> = dataSource.observeFloors()

    override suspend fun addFloor(name: String, order: Int, gridColumns: Int, gridRows: Int, imageUrl: String?) {
        dataSource.addFloor(name, order, gridColumns, gridRows, imageUrl)
    }
}
