package com.example.holoverse.chat.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // "Student" or "Mentor"
    val text: String = "",
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val glbUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val poll: Poll? = null,
    val bookingRequest: BookingRequest? = null,
    val timestamp: Timestamp? = null,
    val status: MessageStatus = MessageStatus.SENT,
    @get:PropertyName("isCallMessage")
    @set:PropertyName("isCallMessage")
    var isCallMessage: Boolean = false
)

enum class MessageStatus {
    SENDING, SENT, FAILED
}

@Serializable
data class BookingRequest(
    val sessionId: String = "",
    val batchId: String = "",
    val proposedTimes: List<Long> = emptyList(), // Store as epoch seconds
    val status: String = "PENDING" // PENDING, CONFIRMED, REJECTED
)

