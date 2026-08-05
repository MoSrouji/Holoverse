package com.example.holoverse.chat.domain.repository

import com.example.holoverse.chat.domain.model.Chat
import com.example.holoverse.chat.domain.model.Message
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
        participantImageUrl: String?,
        mentorId: String? = null,
        mentorName: String? = null
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
        glbUrl: String? = null,
        fileUrl: String? = null,
        fileName: String? = null
    )

    suspend fun sendCallNotification(
        chatId: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String?
    )

    fun createOrGetSupportChat(
        userId: String,
        userName: String,
        userImageUrl: String?,
        admin: com.example.holoverse.auth.domain.entities.User.Admin
    ): Flow<com.example.holoverse.core.utils.Response<String>>

    suspend fun updateGroupSettings(
        chatId: String,
        name: String? = null,
        description: String? = null,
        imageUrl: String? = null,
        isOnlyMentorMessaging: Boolean? = null
    )

    suspend fun leaveGroup(chatId: String, userId: String)

    suspend fun restrictMember(chatId: String, userId: String)
    suspend fun unrestrictMember(chatId: String, userId: String)

    suspend fun sendPoll(chatId: String, question: String, options: List<String>)
    suspend fun voteOnPoll(chatId: String, messageId: String, optionIndex: Int, userId: String)

    suspend fun sendBookingRequest(
        chatId: String,
        batchId: String,
        sessionId: String,
        proposedTimes: List<Long>
    )

    suspend fun respondToBookingRequest(
        chatId: String,
        messageId: String,
        status: String
    )
}

