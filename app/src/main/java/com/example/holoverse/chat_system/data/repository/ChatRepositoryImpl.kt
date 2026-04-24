package com.example.holoverse.chat_system.data.repository

import android.content.Context
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.chat_system.data.local.dao.ChatDao
import com.example.holoverse.chat_system.data.local.dao.MessageDao
import com.example.holoverse.chat_system.data.local.entities.ChatEntity
import com.example.holoverse.chat_system.data.local.entities.MessageEntity
import com.example.holoverse.chat_system.data.remote.FcmApi
import com.example.holoverse.chat_system.data.remote.FcmMessage
import com.example.holoverse.chat_system.data.remote.FcmV1Request
import com.example.holoverse.chat_system.data.remote.NotificationData
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message
import com.example.holoverse.chat_system.domain.repository.ChatRepository
import com.example.holoverse.utils.NetworkConstant
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val authRepository: AuthRepository,
    private val fcmApi: FcmApi,
    @ApplicationContext private val context: Context
) : ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _chatsState = MutableStateFlow<List<Chat>>(emptyList())
    override val chatsState: StateFlow<List<Chat>> = _chatsState.asStateFlow()

    private val _messagesState = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    override val messagesState: StateFlow<Map<String, List<Message>>> = _messagesState.asStateFlow()

    override fun getMessages(chatId: String): Flow<List<Message>> {
        repositoryScope.launch {
            syncMessagesFromRemote(chatId)
        }
        val flow = messageDao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
        repositoryScope.launch {
            flow.collect { messages ->
                _messagesState.value = _messagesState.value.toMutableMap().apply {
                    put(chatId, messages)
                }
            }
        }
        return flow
    }

    private suspend fun syncMessagesFromRemote(chatId: String) {
        if (chatId.isBlank()) return
        try {
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .document(chatId)
                .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()

            val messages = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            }
            if (messages.isNotEmpty()) {
                messageDao.insertMessages(messages.map { it.toEntity(chatId) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getChats(userId: String): Flow<List<Chat>> {
        repositoryScope.launch {
            syncChatsFromRemote(userId)
        }
        val flow = chatDao.getChatsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
        repositoryScope.launch {
            flow.collect { chats ->
                _chatsState.value = chats
            }
        }
        return flow
    }

    private suspend fun syncChatsFromRemote(userId: String) {
        try {
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val chats = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }
            if (chats.isNotEmpty()) {
                chatDao.insertChats(chats.map { it.toEntity() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun createOrGetChat(
        currentUserId: String,
        otherUserId: String,
        currentUserName: String,
        otherUserName: String,
        currentUserImageUrl: String?,
        otherUserImageUrl: String?
    ): String {
        val participants = listOf(currentUserId, otherUserId).sorted()
        val chatId = participants.joinToString("_")

        val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
        val snapshot = chatRef.get().await()

        if (!snapshot.exists()) {
            val namesMap = mutableMapOf(
                currentUserId to currentUserName,
                otherUserId to otherUserName
            )
            val imagesMap = mutableMapOf<String, String>()
            currentUserImageUrl?.let { imagesMap[currentUserId] = it }
            otherUserImageUrl?.let { imagesMap[otherUserId] = it }

            val chatData = Chat(
                id = chatId,
                participants = participants,
                participantNames = namesMap,
                participantProfileImages = imagesMap
            )
            chatRef.set(chatData).await()
            chatDao.insertChats(listOf(chatData.toEntity()))
        }
        return chatId
    }

    override suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        senderType: String,
        audioUrl: String?
    ) {
        val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
        val messageRef = chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document()
        val serverTime = FieldValue.serverTimestamp()
        
        val messageMap = mutableMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "senderType" to senderType,
            "text" to text,
            "timestamp" to serverTime
        )
        audioUrl?.let { messageMap["audioUrl"] = it }

        firestore.runBatch { batch ->
            batch.set(messageRef, messageMap)
            val chatUpdate = mutableMapOf(
                "lastMessage" to if (audioUrl != null && text.isEmpty()) "Audio message" else text,
                "lastMessageTimestamp" to serverTime,
                "lastSenderId" to senderId,
                "lastSenderName" to senderName,
                "participants" to FieldValue.arrayUnion(senderId)
            )
            batch.set(chatRef, chatUpdate, SetOptions.merge())
        }.await()
        
        syncMessagesFromRemote(chatId)
        
        // Trigger Notification using FCM V1
        repositoryScope.launch {
            try {
                val participants = chatId.split("_")
                val recipientId = participants.find { it != senderId } ?: return@launch
                
                val recipientToken = authRepository.getFcmToken(recipientId)
                
                if (!recipientToken.isNullOrBlank()) {
                    val authHeader = getAccessToken()
                    val request = FcmV1Request(
                        message = FcmMessage(
                            token = recipientToken,
                            notification = NotificationData(
                                title = senderName,
                                body = text
                            ),
                            data = mapOf(
                                "chatId" to chatId,
                                "senderId" to senderId
                            )
                        )
                    )
                    fcmApi.sendNotification(authHeader, request)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            val stream = context.assets.open("service-account.json")
            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            credentials.refreshIfExpired()
            "Bearer ${credentials.accessToken.tokenValue}"
        }
    }

    // Helper extensions
    private fun Message.toEntity(chatId: String): MessageEntity {
        return MessageEntity(
            id = this.id,
            chatId = chatId,
            senderId = this.senderId,
            senderName = this.senderName,
            senderType = this.senderType,
            text = this.text,
            audioUrl = this.audioUrl,
            timestamp = this.timestamp?.seconds ?: 0L
        )
    }

    private fun MessageEntity.toDomain(): Message {
        return Message(
            id = this.id,
            senderId = this.senderId,
            senderName = this.senderName,
            senderType = this.senderType,
            text = this.text,
            audioUrl = this.audioUrl,
            timestamp = if (this.timestamp != 0L) Timestamp(this.timestamp, 0) else null
        )
    }

    private fun Chat.toEntity(): ChatEntity {
        return ChatEntity(
            id = this.id,
            participants = this.participants,
            lastMessage = this.lastMessage,
            lastMessageTimestamp = this.lastMessageTimestamp?.seconds ?: 0L,
            lastSenderName = this.lastSenderName,
            lastSenderId = this.lastSenderId,
            participantNames = this.participantNames,
            participantProfileImages = this.participantProfileImages
        )
    }

    private fun ChatEntity.toDomain(): Chat {
        return Chat(
            id = this.id,
            participants = this.participants,
            lastMessage = this.lastMessage,
            lastMessageTimestamp = if (this.lastMessageTimestamp != 0L) Timestamp(this.lastMessageTimestamp, 0) else null,
            lastSenderName = this.lastSenderName,
            lastSenderId = this.lastSenderId,
            participantNames = this.participantNames,
            participantProfileImages = this.participantProfileImages
        )
    }
}
