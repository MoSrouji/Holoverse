package com.example.holoverse.three_d_model.data.local

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class ModelCacheManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getModelPath(
        modelId: String,
        url: String,
        onProgress: ((Float, Long, Long) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        if (url.startsWith("content://") || url.startsWith("file://")) {
            return@withContext url
        }
        
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()

        // Check for cached directory first (for ZIP extracted models)
        val modelDir = File(modelsDir, modelId)
        if (modelDir.exists() && modelDir.isDirectory) {
            // Try manifest first
            val manifestFile = getModelFileFromManifest(modelDir)
            if (manifestFile != null) {
                onProgress?.invoke(1.0f, manifestFile.length(), manifestFile.length())
                return@withContext "file://${manifestFile.absolutePath}"
            }
            
            // Fallback to searching
            val modelFile = findModelFile(modelDir)
            if (modelFile != null) {
                saveManifest(modelDir, modelFile)
                onProgress?.invoke(1.0f, modelFile.length(), modelFile.length())
                return@withContext "file://${modelFile.absolutePath}"
            }
        }

        // Check for legacy single file cache
        val legacyGlb = File(modelsDir, "$modelId.glb")
        if (legacyGlb.exists()) {
            onProgress?.invoke(1.0f, legacyGlb.length(), legacyGlb.length())
            return@withContext "file://${legacyGlb.absolutePath}"
        }
        
        val legacyGltf = File(modelsDir, "$modelId.gltf")
        if (legacyGltf.exists()) {
            onProgress?.invoke(1.0f, legacyGltf.length(), legacyGltf.length())
            return@withContext "file://${legacyGltf.absolutePath}"
        }

        try {
            if (!url.startsWith("http")) {
                throw IllegalArgumentException("Invalid URL: $url")
            }
            downloadAndProcessModel(modelId, url, onProgress)
        } catch (e: Exception) {
            Log.e("ModelCacheManager", "Failed to process model from $url", e)
            throw e
        }
    }

    private fun findModelFile(dir: File): File? {
        // 1. Prefer specific names in the current directory
        val preferredNames = listOf("scene.glb", "scene.gltf")
        for (name in preferredNames) {
            val file = File(dir, name)
            if (file.exists()) return file
        }

        // 2. Check for any glb/gltf in the root directory first (faster than recursive walk)
        dir.listFiles()?.find { it.extension == "glb" || it.extension == "gltf" }?.let { return it }

        // 3. Search recursively if not found in root (limit depth for safety)
        return dir.walkTopDown()
            .maxDepth(3)
            .find { it.extension == "glb" || it.extension == "gltf" }
    }

    private suspend fun downloadAndProcessModel(
        modelId: String,
        url: String,
        onProgress: ((Float, Long, Long) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        Log.d("ModelCacheManager", "Downloading model from: $url")
        val modelsDir = File(context.filesDir, "models")
        modelsDir.mkdirs()
        
        val tempFile = File(modelsDir, "$modelId.tmp")
        var currentRetry = 0
        val maxRetries = 3
        var lastException: Exception? = null

        while (currentRetry < maxRetries) {
            try {
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    
                    val body = response.body ?: throw IOException("Empty response body")
                    val totalSize = body.contentLength()
                    
                    body.byteStream().use { input ->
                        tempFile.outputStream().use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var downloadedSize: Long = 0
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloadedSize += bytesRead
                                if (totalSize > 0) {
                                    val progress = downloadedSize.toFloat() / totalSize
                                    onProgress?.invoke(progress, downloadedSize, totalSize)
                                } else {
                                    onProgress?.invoke(-1f, downloadedSize, -1L)
                                }
                            }
                        }
                    }
                }
                
                // If we reached here, download was successful
                lastException = null
                break 
            } catch (e: Exception) {
                lastException = e
                currentRetry++
                Log.w("ModelCacheManager", "Download attempt $currentRetry failed: ${e.message}")
                if (currentRetry < maxRetries) {
                    kotlinx.coroutines.delay(2000L * currentRetry) // Exponential backoff
                }
            }
        }

        if (lastException != null) {
            if (tempFile.exists()) tempFile.delete()
            throw lastException
        }

        try {
            // Detect if it's a ZIP file
            if (isZipFile(tempFile)) {
                Log.d("ModelCacheManager", "ZIP detected, extracting...")
                val targetDir = File(modelsDir, modelId)
                targetDir.mkdirs()
                extractZip(tempFile, targetDir)
                tempFile.delete()
                
                val modelFile = findModelFile(targetDir)
                    ?: throw Exception("No glTF/GLB found in ZIP")
                
                saveManifest(targetDir, modelFile)
                "file://${modelFile.absolutePath}"
            } else {
                // Assume it's a direct GLB/glTF
                val extension = if (url.contains(".gltf")) "gltf" else "glb"
                val destination = File(modelsDir, "$modelId.$extension")
                if (!tempFile.renameTo(destination)) {
                    // If rename fails, try copying
                    tempFile.copyTo(destination, overwrite = true)
                    tempFile.delete()
                }
                "file://${destination.absolutePath}"
            }
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            throw e
        }
    }

    private fun isZipFile(file: File): Boolean {
        if (file.length() < 4) return false
        val bytes = ByteArray(4)
        file.inputStream().use { it.read(bytes) }
        // ZIP magic number: PK\x03\x04
        return bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && 
               bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
    }

    private fun extractZip(zipFile: File, targetDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                // Prevent ZipSlip vulnerability and handle path traversal
                val newFile = File(targetDir, entry.name).canonicalFile
                if (!newFile.path.startsWith(targetDir.canonicalPath)) {
                    throw IOException("Entry is outside of the target dir: ${entry.name}")
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
    private fun saveManifest(modelDir: File, modelFile: File) {
        try {
            val manifestFile = File(modelDir, "manifest.json")
            val manifest = mapOf(
                "modelFile" to modelFile.name,
                "cachedAt" to System.currentTimeMillis()
            )
            val adapter = moshi.adapter(Map::class.java)
            manifestFile.writeText(adapter.toJson(manifest))
        } catch (e: Exception) {
            Log.e("ModelCacheManager", "Error saving manifest", e)
        }
    }

    private fun getModelFileFromManifest(modelDir: File): File? {
        return try {
            val manifestFile = File(modelDir, "manifest.json")
            if (!manifestFile.exists()) return null
            
            val adapter = moshi.adapter(Map::class.java)
            val manifest = adapter.fromJson(manifestFile.readText())
            val fileName = manifest?.get("modelFile") as? String ?: return null
            File(modelDir, fileName).takeIf { it.exists() }
        } catch (e: Exception) {
            null
        }
    }
}
