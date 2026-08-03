package com.example.holoverse.material.domain.repository

import com.example.holoverse.core.utils.Response
import com.example.holoverse.material.domain.model.Material
import com.example.holoverse.material.domain.model.MaterialType
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    suspend fun saveMaterial(userId: String, material: Material): Result<Unit>
    fun getMaterials(userId: String, type: MaterialType? = null): Flow<Response<List<Material>>>
    suspend fun isMaterialSaved(userId: String, materialId: String): Boolean
}
