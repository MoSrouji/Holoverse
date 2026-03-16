package com.example.holoverse.cloudinary_services.domain.repository

import android.net.Uri

interface CloudinaryRepository {
    suspend fun uploadFile(fileUri: Uri): Result<String>
    fun getPhotoUrl(publicId: String): String
}
