package com.example.holoverse.chat_system.domain.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // "Student" or "Mentor"
    val text: String = "",
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val timestamp: Timestamp? = null,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING, SENT, FAILED
}
