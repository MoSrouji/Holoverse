package com.example.holoverse.payment.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.ENROLLMENT,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class TransactionType {
    MENTOR_SIGNUP,
    MENTOR_UPGRADE,
    COURSE_BOOST,
    ENROLLMENT,
    MODEL_PURCHASE
}
