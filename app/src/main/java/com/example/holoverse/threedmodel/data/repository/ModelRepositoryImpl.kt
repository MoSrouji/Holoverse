package com.example.holoverse.threedmodel.data.repository

import com.example.holoverse.threedmodel.data.remote.ApiService
import com.example.holoverse.threedmodel.data.remote.dto.toModels
import com.example.holoverse.threedmodel.domain.model.Model
import com.example.holoverse.threedmodel.domain.repository.ModelRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ModelRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore
) : ModelRepository {
    private var cachedModels: List<Model>? = null
    private val collection = firestore.collection("threed_models")

    override suspend fun getModels(forceRefresh: Boolean): List<Model> {
        if (!forceRefresh && cachedModels != null) {
            return cachedModels!!
        }

        return try {
            val apiModels = apiService.getModels().toModels()
            
            val firestoreModels = collection.get().await().map { doc ->
                Model(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    path = doc.getString("path") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "Other",
                    imageUrl = doc.getString("imageUrl"),
                    price = doc.getDouble("price") ?: 0.0,
                    uploadedBy = doc.getString("uploadedBy") ?: "Unknown",
                    uploadedById = doc.getString("uploadedById")
                )
            }

            val allModels = apiModels + firestoreModels
            cachedModels = allModels
            allModels
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

    override suspend fun uploadModel(model: Model): Result<Unit> {
        return try {
            val modelData = hashMapOf(
                "name" to model.name,
                "path" to model.path,
                "description" to model.description,
                "category" to model.category,
                "imageUrl" to model.imageUrl,
                "price" to model.price,
                "uploadedBy" to model.uploadedBy,
                "uploadedById" to model.uploadedById,
                "createdAt" to System.currentTimeMillis()
            )
            collection.add(modelData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
