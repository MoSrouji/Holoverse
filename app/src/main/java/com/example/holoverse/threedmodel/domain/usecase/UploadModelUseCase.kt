package com.example.holoverse.threedmodel.domain.usecase

import com.example.holoverse.threedmodel.domain.model.Model
import com.example.holoverse.threedmodel.domain.repository.ModelRepository
import javax.inject.Inject

class UploadModelUseCase @Inject constructor(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(model: Model): Result<Unit> {
        return repository.uploadModel(model)
    }
}
