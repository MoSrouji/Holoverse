package com.example.holoverse.chatsystem.domain.repository

import com.example.holoverse.chatsystem.domain.model.Chat
import com.example.holoverse.chatsystem.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val chatsState: StateFlow<List<Chat>>
    val messagesState: StateFlow<Map<String, List<Message>>>

    fun getMessages(chatId: String): Flow<List<Message>>
    fun getChats(userId: String): Flow<List<Chat>>
    suspend fun createOrGetChat(
        currentUserId: String,
        otherUserId: String,
        currentUserName: String,
        otherUserName: String,
        currentUserImageUrl: String? = null,
        otherUserImageUrl: String? = null
    ): String

    suspend fun createOrJoinGroupChat(
        courseId: String,
        courseName: String,
        courseImageUrl: String?,
        participantId: String,
        participantName: String,
        participantImageUrl: String?
    ): String

    suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        senderType: String,
        audioUrl: String? = null,
        imageUrl: String? = null,
        videoUrl: String? = null,
        fileUrl: String? = null,
        fileName: String? = null
    )

    suspend fun sendCallNotification(
        chatId: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String?
    )
}
