package com.example.holoverse.cloudinaryservices.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.Transformation
import com.example.holoverse.cloudinaryservices.domain.model.CloudinaryUploadResult
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class CloudinaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CloudinaryRepository {

    override suspend fun uploadFile(fileUri: Uri): Result<CloudinaryUploadResult> {
        return suspendCancellableCoroutine { continuation ->
            // Better detection for GLB files from content URIs
            val isGlb = try {
                val fileName = getFileName(fileUri)
                fileName?.lowercase()?.endsWith(".glb") == true
            } catch (e: Exception) {
                fileUri.toString().lowercase().endsWith(".glb")
            }
            
            val resourceType = if (isGlb) "image" else "auto"
            
            MediaManager.get().upload(fileUri)
                .unsigned("ml_default")
                .option("resource_type", resourceType)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("Cloudinary", "Upload started")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String
                        val publicId = resultData?.get("public_id") as? String
                        
                        Log.d("Cloudinary", "Upload success! URL: $url, Public ID: $publicId")

                        if (url != null && publicId != null) {
                            continuation.resume(Result.success(CloudinaryUploadResult(url, publicId)))
                        } else {
                            continuation.resume(Result.failure(Exception("Upload success but URL or Public ID is null")))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("Cloudinary", "Upload error: ${error?.description}")
                        continuation.resume(Result.failure(Exception(error?.description ?: "Unknown error")))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(Result.failure(Exception("Upload rescheduled: ${error?.description}")))
                    }
                }).dispatch()
        }
    }

    override fun getPhotoUrl(publicId: String): String {
        return MediaManager.get().url().generate(publicId)
    }

    override fun get3DThumbnailUrl(publicId: String): String {
        val url = MediaManager.get().url()
            .secure(true)
            .resourceType("image")
            .transformation(Transformation<Transformation<*>>()
                .effect("camera:up_45;right_30;zoom_1.0")
                .fetchFormat("auto")
                .quality("auto"))
            .generate(publicId + ".jpg")
        Log.d("Cloudinary", "Generated Thumbnail URL: $url")
        return url
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
