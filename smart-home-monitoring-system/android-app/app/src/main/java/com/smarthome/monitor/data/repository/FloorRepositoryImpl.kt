package com.smarthome.monitor.data.repository

import android.content.Context
import com.smarthome.monitor.data.local.FloorImageCache
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.data.remote.FirebaseFloorDataSource
import com.smarthome.monitor.data.remote.FirebaseStorageUploader
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloorRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFloorDataSource,
    private val storageUploader: FirebaseStorageUploader,
    private val imageCache: FloorImageCache,
    @ApplicationContext private val context: Context
) : FloorRepository {
    override fun observeFloors(): Flow<List<Floor>> = dataSource.observeFloors()

    override suspend fun addFloor(
        name: String,
        order: Int,
        gridColumns: Int,
        gridRows: Int,
        imageUrl: String?,
        localImagePath: String?
    ): String = withContext(Dispatchers.IO) {
        android.util.Log.d("FloorRepo", "addFloor localImagePath=$localImagePath")
        if (localImagePath != null) {
            val floorId = dataSource.addFloor(name, order, gridColumns, gridRows, "local")
            copyToPermanentStorage(localImagePath, floorId)
            android.util.Log.d(
                "FloorRepo",
                "copied to floor_plans for floorId=$floorId -> ${File(context.filesDir, "floor_plans").listFiles()?.map { it.name }}"
            )
            floorId
        } else {
            dataSource.addFloor(name, order, gridColumns, gridRows, imageUrl)
        }
    }

    private fun copyToPermanentStorage(localImagePath: String, floorId: String) {
        val source = File(localImagePath)
        if (!source.exists()) return
        val extension = source.extension.ifBlank { "jpg" }
        val dir = File(context.filesDir, "floor_plans").apply { mkdirs() }
        val target = File(dir, "$floorId.$extension")
        source.copyTo(target, overwrite = true)
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
