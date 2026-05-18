package com.example.holoverse.three_d_model.data.repository

import com.example.holoverse.three_d_model.data.remote.ApiService
    import com.example.holoverse.three_d_model.data.remote.dto.toModels
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
            val models = apiService.getModels().toModels()

            cachedModels = models
            models
        } catch (e: Exception) {
            cachedModels ?: emptyList()
        }
    }

    override suspend fun searchModels(query: String): List<Model> {
        // Filter models locally based on query
        return cachedModels?.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) 
        } ?: emptyList()
    }

    override suspend fun getDownloadUrl(modelId: String): String? {
        // Models have the direct URL in the path.
        return cachedModels?.find { it.id == modelId }?.path
    }
}



