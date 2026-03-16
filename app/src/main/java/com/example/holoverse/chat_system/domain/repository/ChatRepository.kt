package com.example.holoverse.chat_system.domain.repository

import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val chatsState: StateFlow<List<Chat>>
    val messagesState: StateFlow<Map<String, List<Message>>>

    fun getMessages(chatId: String): Flow<List<Message>>
    fun getChats(userId: String): Flow<List<Chat>>
    suspend fun createOrGetChat(currentUserId: String, otherUserId: String, currentUserName: String, otherUserName: String): String
    suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        senderType: String,
        audioUrl: String? = null
    )
}
