package com.example.holoverse.chat.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.example.holoverse.chat.domain.model.MessageStatus

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
    val glbUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val poll: com.example.holoverse.chat.domain.model.Poll? = null,
    val bookingRequest: com.example.holoverse.chat.domain.model.BookingRequest? = null,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)

