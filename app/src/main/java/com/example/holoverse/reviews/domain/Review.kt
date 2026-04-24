package com.example.holoverse.reviews.domain

data class Review(
    val id: String = "",
    val targetId: String = "", // Course ID or Mentor ID
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
