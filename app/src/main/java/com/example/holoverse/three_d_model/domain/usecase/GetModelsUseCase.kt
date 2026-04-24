package com.example.holoverse.three_d_model.domain.usecase

import com.example.holoverse.three_d_model.domain.model.Model
import com.example.holoverse.three_d_model.domain.repository.ModelRepository
import javax.inject.Inject

class GetModelsUseCase @Inject constructor(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): List<Model> {
        return repository.getModels(forceRefresh)
    }
}
