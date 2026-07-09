package com.example.holoverse.threedmodel.domain.usecase

import com.example.holoverse.threedmodel.domain.model.Model
import com.example.holoverse.threedmodel.domain.repository.ModelRepository
import javax.inject.Inject

class GetModelsUseCase @Inject constructor(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): List<Model> {
        return repository.getModels(forceRefresh)
    }
}
