package com.example.holoverse.reviews.data

import com.example.holoverse.reviews.domain.Review
import com.example.holoverse.reviews.domain.ReviewRepository
import com.example.holoverse.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ReviewRepositoryImpl(private val firestore: FirebaseFirestore) : ReviewRepository {

    override suspend fun addReview(review: Review): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            // Check if user already reviewed this target
            val existingReviews = firestore.collection("reviews")
                .whereEqualTo("targetId", review.targetId)
                .whereEqualTo("userId", review.userId)
                .get()
                .await()

            if (!existingReviews.isEmpty) {
                // Update existing review
                val docId = existingReviews.documents[0].id
                firestore.collection("reviews").document(docId)
                    .set(review.copy(id = docId, createdAt = System.currentTimeMillis()))
                    .await()
            } else {
                // Add new review
                val reviewRef = firestore.collection("reviews").document()
                firestore.collection("reviews").document(reviewRef.id)
                    .set(review.copy(id = reviewRef.id))
                    .await()
            }
            
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Failed to add review"))
        }
    }

    override suspend fun getReviewsByTargetId(targetId: String): Flow<Response<List<Review>>> = callbackFlow {
        trySend(Response.Loading)
        val subscription = firestore.collection("reviews")
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Error fetching reviews"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    trySend(Response.Success(reviews))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getAverageRating(targetId: String): Flow<Response<Double>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("targetId", targetId)
                .get()
                .await()
            
            val reviews = snapshot.toObjects(Review::class.java)
            if (reviews.isEmpty()) {
                emit(Response.Success(0.0))
            } else {
                val average = reviews.map { it.rating }.average()
                emit(Response.Success(average))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Error calculating average rating"))
        }
    }
}
