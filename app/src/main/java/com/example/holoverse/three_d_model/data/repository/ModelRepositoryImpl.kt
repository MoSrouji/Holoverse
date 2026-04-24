package com.example.holoverse.three_d_model.data.repository

import com.example.holoverse.three_d_model.data.remote.ApiService
import com.example.holoverse.three_d_model.data.remote.dto.toModel
import com.example.holoverse.three_d_model.domain.model.Model
import com.example.holoverse.three_d_model.domain.repository.ModelRepository
import javax.inject.Inject

class ModelRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ModelRepository {
    private var cachedModels: List<Model>? = null

    override suspend fun getModels(forceRefresh: Boolean): List<Model> {
        if (!forceRefresh && cachedModels != null) {
            return cachedModels!!
        }

        return try {
            val models = apiService.getKhronosModels()
                .mapNotNull { it.toModel() }
                .take(100)
            cachedModels = models
            models
        } catch (e: Exception) {
            cachedModels ?: emptyList()
        }
    }
}


