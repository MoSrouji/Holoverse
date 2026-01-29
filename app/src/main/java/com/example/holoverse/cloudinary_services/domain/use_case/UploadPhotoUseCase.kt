package com.example.holoverse.cloudinary_services.domain.use_case

import android.net.Uri
import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: CloudinaryRepository
) {
    suspend operator fun invoke(fileUri: Uri): Result<String> {
        return repository.uploadPhoto(fileUri)
    }
}
