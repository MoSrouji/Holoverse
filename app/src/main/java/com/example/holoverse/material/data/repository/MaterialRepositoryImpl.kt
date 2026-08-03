package com.example.holoverse.material.data.repository

import com.example.holoverse.core.utils.Response
import com.example.holoverse.material.domain.model.Material
import com.example.holoverse.material.domain.model.MaterialType
import com.example.holoverse.material.domain.repository.MaterialRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MaterialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MaterialRepository {

    private fun getCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("user_materials")

    override suspend fun saveMaterial(userId: String, material: Material): Result<Unit> {
        return try {
            getCollection(userId).document(material.id).set(material).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMaterials(userId: String, type: MaterialType?): Flow<Response<List<Material>>> = callbackFlow {
        trySend(Response.Loading)
        
        var query: Query = getCollection(userId).orderBy("savedAt", Query.Direction.DESCENDING)
        
        if (type != null) {
            query = query.whereEqualTo("type", type.name)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.message ?: "Unknown error"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val materials = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Material::class.java)
                }
                trySend(Response.Success(materials))
            }
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun isMaterialSaved(userId: String, materialId: String): Boolean {
        return try {
            val doc = getCollection(userId).document(materialId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
