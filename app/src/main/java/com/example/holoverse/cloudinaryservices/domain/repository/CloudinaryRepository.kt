package com.example.holoverse.cloudinaryservices.domain.repository

import android.net.Uri
import com.example.holoverse.cloudinaryservices.domain.model.CloudinaryUploadResult

interface CloudinaryRepository {
    suspend fun uploadFile(fileUri: Uri): Result<CloudinaryUploadResult>
    fun getPhotoUrl(publicId: String): String
    fun get3DThumbnailUrl(publicId: String): String
}
