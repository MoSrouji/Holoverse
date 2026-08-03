package com.example.holoverse.chat.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val participants: List<String>,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val lastSenderName: String,
    val lastSenderId: String,
    val participantNames: Map<String, String>,
    val participantProfileImages: Map<String, String>,
    val isSupportChat: Boolean = false,
    val creatorId: String? = null,
    val groupDescription: String? = null,
    val restrictedParticipants: List<String> = emptyList(),
    val isOnlyMentorMessaging: Boolean = false
) {
    val isGroup: Boolean get() = id.startsWith("group_")
}

