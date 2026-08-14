package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.local.FloorImageCache
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.data.remote.FirebaseFloorDataSource
import com.smarthome.monitor.data.remote.FirebaseStorageUploader
import com.smarthome.monitor.domain.repository.FloorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloorRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFloorDataSource,
    private val storageUploader: FirebaseStorageUploader,
    private val imageCache: FloorImageCache
) : FloorRepository {
    override fun observeFloors(): Flow<List<Floor>> = dataSource.observeFloors()

    override suspend fun addFloor(name: String, order: Int, gridColumns: Int, gridRows: Int, imageUrl: String?) {
        dataSource.addFloor(name, order, gridColumns, gridRows, imageUrl)
    }

    override suspend fun cacheFloorImage(contentUri: String): String? =
        withContext(Dispatchers.IO) { imageCache.cacheFromContentUri(contentUri) }

    override suspend fun uploadFloorImage(localImage: String): String? {
        return if (localImage.startsWith("content://") || localImage.startsWith("/")) {
            storageUploader.uploadFloorPlanImage(localImage)
        } else {
            null
        }
    }
}
