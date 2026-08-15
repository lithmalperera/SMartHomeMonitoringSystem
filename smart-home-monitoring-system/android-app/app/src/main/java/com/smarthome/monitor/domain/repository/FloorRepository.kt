package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Floor
import kotlinx.coroutines.flow.Flow

interface FloorRepository {
    fun observeFloors(): Flow<List<Floor>>
    suspend fun addFloor(
        name: String,
        order: Int,
        gridColumns: Int,
        gridRows: Int,
        imageUrl: String?,
        localImagePath: String? = null
    ): String
    suspend fun cacheFloorImage(contentUri: String): String?
    suspend fun uploadFloorImage(localImage: String): String?
}
