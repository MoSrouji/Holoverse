package com.example.holoverse.chat_system.data.local.entities

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
    val participantNames: Map<String, String>
)
