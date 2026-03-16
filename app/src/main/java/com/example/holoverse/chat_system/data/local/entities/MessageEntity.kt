package com.example.holoverse.chat_system.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderType: String,
    val text: String,
    val audioUrl: String? = null,
    val timestamp: Long
)
