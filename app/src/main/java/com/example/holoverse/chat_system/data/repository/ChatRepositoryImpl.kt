package com.example.holoverse.chat_system.data.repository

import android.content.Context
import android.util.Log
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.chat_system.data.local.dao.ChatDao
import com.example.holoverse.chat_system.data.local.dao.MessageDao
import com.example.holoverse.chat_system.data.local.entities.ChatEntity
import com.example.holoverse.chat_system.data.local.entities.MessageEntity
import com.example.holoverse.chat_system.data.remote.AndroidConfig
import com.example.holoverse.chat_system.data.remote.AndroidNotification
import com.example.holoverse.chat_system.data.remote.FcmApi
import com.example.holoverse.chat_system.data.remote.FcmMessage
import com.example.holoverse.chat_system.data.remote.FcmV1Request
import com.example.holoverse.chat_system.data.remote.NotificationData
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message
import com.example.holoverse.chat_system.domain.model.MessageStatus
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
import java.util.UUID
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

        // 1. Check local first
        val localChat = chatDao.getChatById(chatId)
        if (localChat != null) {
            return chatId
        }

        // 2. Try remote if not found locally
        try {
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
            } else {
                val remoteChat = snapshot.toObject(Chat::class.java)?.copy(id = snapshot.id)
                remoteChat?.let { chatDao.insertChats(listOf(it.toEntity())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In offline mode, if it's not in local DB, create a temporary local entry
            val namesMap = mutableMapOf(
                currentUserId to currentUserName,
                otherUserId to otherUserName
            )
            val imagesMap = mutableMapOf<String, String>()
            currentUserImageUrl?.let { imagesMap[currentUserId] = it }
            otherUserImageUrl?.let { imagesMap[otherUserId] = it }

            val tempChat = Chat(
                id = chatId,
                participants = participants,
                participantNames = namesMap,
                participantProfileImages = imagesMap
            )
            chatDao.insertChats(listOf(tempChat.toEntity()))
        }
        return chatId
    }

    override suspend fun createOrJoinGroupChat(
        courseId: String,
        courseName: String,
        courseImageUrl: String?,
        participantId: String,
        participantName: String,
        participantImageUrl: String?
    ): String {
        val chatId = "group_$courseId"

        try {
            val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            val snapshot = chatRef.get().await()

            if (!snapshot.exists()) {
                val chatData = Chat(
                    id = chatId,
                    participants = listOf(participantId),
                    participantNames = mapOf(participantId to participantName, chatId to "$courseName Group"),
                    participantProfileImages = participantImageUrl?.let { mapOf(participantId to it) } ?: emptyMap(),
                    lastMessage = "Group created for $courseName",
                    lastMessageTimestamp = Timestamp.now()
                )
                chatRef.set(chatData).await()
                chatDao.insertChats(listOf(chatData.toEntity()))
            } else {
                // Add participant to existing group
                val updateData = mutableMapOf<String, Any>(
                    "participants" to FieldValue.arrayUnion(participantId),
                    "participantNames.$participantId" to participantName
                )
                participantImageUrl?.let {
                    updateData["participantProfileImages.$participantId"] = it
                }

                chatRef.update(updateData).await()
                
                // Refresh local chat
                val updatedSnapshot = chatRef.get().await()
                val remoteChat = updatedSnapshot.toObject(Chat::class.java)?.copy(id = updatedSnapshot.id)
                remoteChat?.let { chatDao.insertChats(listOf(it.toEntity())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chatId
    }

    override suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        senderType: String,
        audioUrl: String?,
        imageUrl: String?,
        videoUrl: String?,
        fileUrl: String?,
        fileName: String?
    ) {
        val messageId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        
        // 1. Create local message entity with SENDING status
        val localMessage = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderType = senderType,
            text = text,
            audioUrl = audioUrl,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            fileUrl = fileUrl,
            fileName = fileName,
            timestamp = currentTime / 1000,
            status = MessageStatus.SENDING
        )
        
        // 2. Save to local DB immediately
        messageDao.insertMessages(listOf(localMessage))
        
        // 3. Update local chat last message
        val lastMessageText = when {
            imageUrl != null -> "Image"
            videoUrl != null -> "Video"
            fileUrl != null -> fileName ?: "Document"
            audioUrl != null && text.isEmpty() -> "Audio message"
            else -> text
        }
        
        chatDao.getChatById(chatId)?.let { currentChat ->
            chatDao.insertChats(listOf(currentChat.copy(
                lastMessage = lastMessageText,
                lastMessageTimestamp = currentTime / 1000,
                lastSenderId = senderId,
                lastSenderName = senderName
            )))
        }

        // 4. Attempt remote sync in background
        repositoryScope.launch {
            try {
                val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
                val messageRef = chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document(messageId)
                val serverTime = FieldValue.serverTimestamp()
                
                val messageMap = mutableMapOf(
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "senderType" to senderType,
                    "text" to text,
                    "timestamp" to serverTime
                )
                audioUrl?.let { messageMap["audioUrl"] = it }
                imageUrl?.let { messageMap["imageUrl"] = it }
                videoUrl?.let { messageMap["videoUrl"] = it }
                fileUrl?.let { messageMap["fileUrl"] = it }
                fileName?.let { messageMap["fileName"] = it }

                firestore.runBatch { batch ->
                    batch.set(messageRef, messageMap)
                    val chatUpdate = mutableMapOf(
                        "lastMessage" to lastMessageText,
                        "lastMessageTimestamp" to serverTime,
                        "lastSenderId" to senderId,
                        "lastSenderName" to senderName
                    )
                    if (!chatId.startsWith("group_")) {
                        chatUpdate["participants"] = FieldValue.arrayUnion(senderId)
                    }
                    batch.set(chatRef, chatUpdate, SetOptions.merge())
                }.await()
                
                // Update local status to SENT
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.SENT)))
                
                // Trigger Notification
                try {
                    val participants = chatId.split("_")
                    val recipientId = participants.find { it != senderId } ?: return@launch
                    
                    val recipientToken = authRepository.getFcmToken(recipientId)
                    
                    if (!recipientToken.isNullOrBlank()) {
                        try {
                            val authHeader = getAccessToken()
                            val request = FcmV1Request(
                                message = FcmMessage(
                                    token = recipientToken,
                                    notification = NotificationData(
                                        title = senderName,
                                        body = lastMessageText
                                    ),
                                    data = mapOf(
                                        "chatId" to chatId,
                                        "senderId" to senderId
                                    ),
                                    android = AndroidConfig(
                                        priority = "high",
                                        notification = AndroidNotification(
                                            channel_id = "chat_notifications",
                                            notification_priority = "PRIORITY_HIGH"
                                        )
                                    )
                                )
                            )
                            fcmApi.sendNotification(authHeader, request)
                        } catch (e: Exception) {
                            Log.e("ChatRepository", "Failed to send notification: ${e.message}")
                            // We don't mark the message as failed here because the message 
                            // was already successfully sent to Firestore.
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Update status to FAILED if offline/error
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.FAILED)))
            }
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val stream = context.assets.open("service-account.json")
                val credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                credentials.refreshIfExpired()
                "Bearer ${credentials.accessToken.tokenValue}"
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error getting access token: ${e.message}. " +
                        "Ensure 'service-account.json' is in assets folder.")
                throw e
            }
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
            imageUrl = this.imageUrl,
            videoUrl = this.videoUrl,
            fileUrl = this.fileUrl,
            fileName = this.fileName,
            timestamp = this.timestamp?.seconds ?: (System.currentTimeMillis() / 1000),
            status = this.status
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
            imageUrl = this.imageUrl,
            videoUrl = this.videoUrl,
            fileUrl = this.fileUrl,
            fileName = this.fileName,
            timestamp = if (this.timestamp != 0L) Timestamp(this.timestamp, 0) else null,
            status = this.status
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
