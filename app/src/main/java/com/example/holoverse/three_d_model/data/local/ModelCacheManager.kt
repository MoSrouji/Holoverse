package com.example.holoverse.three_d_model.data.local

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class ModelCacheManager(private val context: Context) {

    suspend fun getModelPath(modelId: String, url: String): String {
        if (url.startsWith("content://") || url.startsWith("file://")) {
            return url
        }
        val extension = if (url.endsWith(".gltf")) "gltf" else "glb"
        val fileName = "$modelId.$extension"
        val localFile = File(context.filesDir, "models/$fileName")

        if (localFile.exists()) {
            Log.d("ModelCacheManager", "Using cached model: ${localFile.absolutePath}")
            return "file://${localFile.absolutePath}"
        }

        return try {
            downloadModel(url, localFile)
            "file://${localFile.absolutePath}"
        } catch (e: Exception) {
            Log.e("ModelCacheManager", "Failed to download model", e)
            url // Fallback to URL if download fails
        }
    }

    private suspend fun downloadModel(url: String, destination: File) = withContext(Dispatchers.IO) {
        Log.d("ModelCacheManager", "Downloading model from: $url")
        destination.parentFile?.mkdirs()
        URL(url).openStream().use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.d("ModelCacheManager", "Download complete: ${destination.absolutePath}")
    }
}
