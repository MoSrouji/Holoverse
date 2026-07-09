package com.example.holoverse.cloudinaryservices.domain.use_case

import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import javax.inject.Inject

class GetPhotoUrlUseCase @Inject constructor(
    private val repository: CloudinaryRepository
) {
    operator fun invoke(publicId: String): String {
        return repository.getPhotoUrl(publicId)
    }
}
