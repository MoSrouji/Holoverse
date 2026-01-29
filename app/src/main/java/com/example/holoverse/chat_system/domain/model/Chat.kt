package com.example.holoverse.chat_system.domain.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val lastSenderName: String = "",
    val lastSenderId: String = "",
    val participantNames: Map<String, String> = emptyMap() // Map of userId to fullName
)
