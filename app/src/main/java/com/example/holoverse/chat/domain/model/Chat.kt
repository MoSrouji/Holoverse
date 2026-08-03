package com.example.holoverse.chat.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val lastSenderName: String = "",
    val lastSenderId: String = "",
    val participantNames: Map<String, String> = emptyMap(), // Map of userId to fullName
    val participantProfileImages: Map<String, String> = emptyMap(), // Map of userId to imageUrl
    val isSupportChat: Boolean = false,
    val creatorId: String? = null,
    val groupDescription: String? = null,
    val restrictedParticipants: List<String> = emptyList(),
    @get:PropertyName("onlyMentorMessaging")
    @PropertyName("onlyMentorMessaging")
    val isOnlyMentorMessaging: Boolean = false
) {
    val isGroup: Boolean get() = id.startsWith("group_")
}

