package com.example.holoverse.chat_system.domain.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // "Student" or "Teacher"
    val text: String = "",
    val timestamp: Timestamp? = null
)
