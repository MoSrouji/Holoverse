package com.example.holoverse.chat_system.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.example.holoverse.chat_system.domain.model.MessageStatus

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
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)
