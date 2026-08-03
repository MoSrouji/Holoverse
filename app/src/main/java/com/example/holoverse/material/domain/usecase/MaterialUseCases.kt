package com.example.holoverse.material.domain.usecase

import com.example.holoverse.material.domain.model.Material
import com.example.holoverse.material.domain.model.MaterialType
import com.example.holoverse.material.domain.repository.MaterialRepository
import javax.inject.Inject

class SaveMaterialUseCase @Inject constructor(
    private val repository: MaterialRepository
) {
    suspend operator fun invoke(userId: String, material: Material) = 
        repository.saveMaterial(userId, material)
}

class GetMaterialsUseCase @Inject constructor(
    private val repository: MaterialRepository
) {
    operator fun invoke(userId: String, type: MaterialType? = null) = 
        repository.getMaterials(userId, type)
}

class CheckMaterialSavedUseCase @Inject constructor(
    private val repository: MaterialRepository
) {
    suspend operator fun invoke(userId: String, materialId: String) = 
        repository.isMaterialSaved(userId, materialId)
}
