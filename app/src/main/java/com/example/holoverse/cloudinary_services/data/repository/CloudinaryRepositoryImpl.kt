package com.example.holoverse.cloudinary_services.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class CloudinaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CloudinaryRepository {

    override suspend fun uploadPhoto(fileUri: Uri): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(fileUri)
                .unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("Cloudinary", "Upload started")

                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String
                        Log.d("Cloudinary", "Upload success! URL: $url") // Check this in Logcat

                        if (url != null) {
                            continuation.resume(Result.success(url))
                        } else {
                            continuation.resume(Result.failure(Exception("Upload success but URL is null")))
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
}
