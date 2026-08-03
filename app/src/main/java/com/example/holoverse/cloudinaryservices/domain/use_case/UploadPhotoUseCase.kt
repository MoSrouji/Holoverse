package com.example.holoverse.cloudinaryservices.domain.use_case

import android.net.Uri
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: CloudinaryRepository
) {
    suspend operator fun invoke(fileUri: Uri): Result<String> {
        return repository.uploadFile(fileUri).map { it.url }
    }
}
