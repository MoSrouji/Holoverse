package com.example.holoverse.reviews.domain

import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun addReview(review: Review): Flow<Response<Boolean>>
    suspend fun getReviewsByTargetId(targetId: String): Flow<Response<List<Review>>>
    suspend fun getAverageRating(targetId: String): Flow<Response<Double>>
}

