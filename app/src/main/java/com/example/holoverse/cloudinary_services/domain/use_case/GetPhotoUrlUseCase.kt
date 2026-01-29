package com.example.holoverse.cloudinary_services.domain.use_case

import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import javax.inject.Inject

class GetPhotoUrlUseCase @Inject constructor(
    private val repository: CloudinaryRepository
) {
    operator fun invoke(publicId: String): String {
        return repository.getPhotoUrl(publicId)
    }
}
