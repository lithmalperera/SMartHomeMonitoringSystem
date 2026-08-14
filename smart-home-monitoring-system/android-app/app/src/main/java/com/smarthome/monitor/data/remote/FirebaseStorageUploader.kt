package com.smarthome.monitor.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageUploader @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadFloorPlanImage(localImage: String): String {
        val isContentUri = localImage.startsWith("content://")
        val uri = if (isContentUri) Uri.parse(localImage) else Uri.fromFile(File(localImage))
        val extension = if (isContentUri) {
            inferExtension(uri) ?: "jpg"
        } else {
            File(localImage).extension.ifBlank { "jpg" }
        }
        val ref = storage.reference.child("floor_plans/floor_${System.currentTimeMillis()}.$extension")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun inferExtension(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri) ?: return null
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/svg+xml" -> "svg"
            "image/webp" -> "webp"
            else -> null
        }
    }
}
