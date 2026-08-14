package com.smarthome.monitor.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloorImageCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun cacheFromContentUri(contentUri: String): String? {
        return try {
            val uri = Uri.parse(contentUri)
            val extension = when (context.contentResolver.getType(uri)) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/svg+xml" -> "svg"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val dir = File(context.cacheDir, "floor_images").apply { mkdirs() }
            val file = File(dir, "picked_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) file.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }
}
