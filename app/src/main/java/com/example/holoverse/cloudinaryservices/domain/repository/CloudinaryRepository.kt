package com.example.holoverse.cloudinaryservices.domain.repository

import android.net.Uri

interface CloudinaryRepository {
    suspend fun uploadFile(fileUri: Uri): Result<String>
    fun getPhotoUrl(publicId: String): String
}
