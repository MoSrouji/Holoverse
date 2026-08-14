package com.example.holoverse.chat.data.repository

import android.content.Context
import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.data.local.dao.ChatDao
import com.example.holoverse.chat.data.local.dao.MessageDao
import com.example.holoverse.chat.data.local.entities.ChatEntity
import com.example.holoverse.chat.data.local.entities.MessageEntity
import com.example.holoverse.chat.data.remote.AndroidConfig
import com.example.holoverse.chat.data.remote.AndroidNotification
import com.example.holoverse.chat.data.remote.FcmApi
import com.example.holoverse.chat.data.remote.FcmMessage
import com.example.holoverse.chat.data.remote.FcmV1Request
import com.example.holoverse.chat.data.remote.NotificationData
import com.example.holoverse.chat.domain.model.BookingRequest
import com.example.holoverse.chat.domain.model.Chat
import com.example.holoverse.chat.domain.model.Message
import com.example.holoverse.chat.domain.model.MessageStatus
import com.example.holoverse.chat.domain.model.Poll
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.core.utils.NetworkConstant
import com.example.holoverse.core.utils.Response
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
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

    private val messageListeners = mutableMapOf<String, ListenerRegistration>()
    private var chatsListener: ListenerRegistration? = null

    override fun createOrGetSupportChat(
        userId: String,
        userName: String,
        userImageUrl: String?,
        admin: User.Admin
    ): Flow<Response<String>> = flow {
        emit(Response.Loading)
        try {
            val adminId = admin.userId ?: ""
            val participants = listOf(userId, adminId).sorted()
            val chatId = "support_${participants.joinToString("_")}"

            val chatRef =
                firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            val snapshot = chatRef.get().await()

            if (!snapshot.exists()) {
                val namesMap = mutableMapOf(
                    userId to userName,
                    adminId to (admin.fullName ?: "Admin")
                )
                val imagesMap = mutableMapOf<String, String>()
                userImageUrl?.let { imagesMap[userId] = it }
                admin.profileImageUrl?.let { imagesMap[adminId] = it }

                val chatData = Chat(
                    id = chatId,
                    participants = participants,
                    participantNames = namesMap,
                    participantProfileImages = imagesMap,
                    isSupportChat = true
                )
                chatRef.set(chatData).await()
                withContext(Dispatchers.IO) {
                    chatDao.insertChats(listOf(chatData.toEntity()))
                }
            } else {
                val remoteChat = snapshot.toObject(Chat::class.java)?.copy(id = snapshot.id)
                remoteChat?.let {
                    withContext(Dispatchers.IO) {
                        chatDao.insertChats(listOf(it.toEntity()))
                    }
                }
            }
            emit(Response.Success(chatId))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to create support chat"))
        }
    }

    override fun getMessages(chatId: String): Flow<List<Message>> {
        // Start remote listener if not already started
        if (!messageListeners.containsKey(chatId)) {
            val listener = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .document(chatId)
                .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Error listening for messages: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val messages = it.documents.mapNotNull { doc ->
                            doc.toObject(Message::class.java)?.copy(id = doc.id)
                        }
                        repositoryScope.launch {
                            messageDao.insertMessages(messages.map { m -> m.toEntity(chatId) })
                        }
                    }
                }
            messageListeners[chatId] = listener
        }

        // Return local flow and update messagesState
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

    override fun getChats(userId: String): Flow<List<Chat>> {
        // Start remote listener for chats if not already started
        if (chatsListener == null) {
            chatsListener = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Error listening for chats: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val chats = it.documents.mapNotNull { doc ->
                            doc.toObject(Chat::class.java)?.copy(id = doc.id)
                        }
                        repositoryScope.launch {
                            chatDao.insertChats(chats.map { it.toEntity() })
                        }
                    }
                }
        }

        // Return local flow and update chatsState
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
            val chatRef =
                firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
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
        participantImageUrl: String?,
        mentorId: String?,
        mentorName: String?
    ): String {
        val chatId = "group_$courseId"

        try {
            val chatRef =
                firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            val snapshot = chatRef.get().await()

            if (!snapshot.exists()) {
                val participants = mutableListOf(participantId)
                val namesMap = mutableMapOf(
                    participantId to participantName,
                    chatId to "$courseName Group"
                )
                val imagesMap = mutableMapOf<String, String>()
                participantImageUrl?.let { imagesMap[participantId] = it }

                // Add mentor if provided and not the same as participant
                if (mentorId != null && mentorId != participantId) {
                    participants.add(mentorId)
                    namesMap[mentorId] = mentorName ?: "Mentor"
                }

                val chatData = Chat(
                    id = chatId,
                    participants = participants,
                    participantNames = namesMap,
                    participantProfileImages = imagesMap,
                    lastMessage = "Group created for $courseName",
                    lastMessageTimestamp = Timestamp.now(),
                    creatorId = mentorId ?: participantId
                )
                chatRef.set(chatData).await()
                chatDao.insertChats(listOf(chatData.toEntity()))
            } else {
                // Add participant to existing group
                val remoteChat = snapshot.toObject(Chat::class.java)
                val updateData = mutableMapOf<String, Any>(
                    "participants" to FieldValue.arrayUnion(participantId),
                    "participantNames.$participantId" to participantName
                )
                participantImageUrl?.let {
                    updateData["participantProfileImages.$participantId"] = it
                }

                // If creatorId is missing, set it (helpful for migration)
                if (remoteChat?.creatorId == null) {
                    updateData["creatorId"] = mentorId ?: participantId
                }

                chatRef.update(updateData).await()

                // Refresh local chat
                val updatedSnapshot = chatRef.get().await()
                val updatedChat =
                    updatedSnapshot.toObject(Chat::class.java)?.copy(id = updatedSnapshot.id)
                updatedChat?.let { chatDao.insertChats(listOf(it.toEntity())) }
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
        glbUrl: String?,
        fileUrl: String?,
        fileName: String?,
        isCallMessage: Boolean
    ) {
        // Restriction Check for non-call messages
        if (!isCallMessage) {
            val chat = chatDao.getChatById(chatId)
            if (chat != null && chat.isGroup) {
                if (chat.isOnlyMentorMessaging && chat.creatorId != senderId) {
                    throw IllegalStateException("Only the mentor can send messages in this group.")
                }
                if (chat.restrictedParticipants.contains(senderId)) {
                    throw IllegalStateException("You are restricted from sending messages in this group.")
                }
            }
        }

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
            glbUrl = glbUrl,
            fileUrl = fileUrl,
            fileName = fileName,
            timestamp = currentTime / 1000,
            status = MessageStatus.SENDING,
            isCallMessage = isCallMessage
        )

        // 2. Save to local DB immediately
        messageDao.insertMessages(listOf(localMessage))

        val lastMessageText = when {
            imageUrl != null -> "Image"
            videoUrl != null -> "Video"
            glbUrl != null -> "3D Model"
            fileUrl != null -> fileName ?: "Document"
            audioUrl != null && text.isEmpty() -> "Audio message"
            else -> text
        }

        if (isCallMessage) {
            // Skip last message update and notification for in-call messages
        } else {
            // 3. Update local chat last message
            chatDao.getChatById(chatId)?.let { currentChat ->
                chatDao.insertChats(
                    listOf(
                        currentChat.copy(
                            lastMessage = lastMessageText,
                            lastMessageTimestamp = currentTime / 1000,
                            lastSenderId = senderId,
                            lastSenderName = senderName
                        )
                    )
                )
            }
        }

        // 4. Attempt remote sync in background
        repositoryScope.launch {
            try {
                val chatRef =
                    firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
                val messageRef =
                    chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document(messageId)
                val serverTime = FieldValue.serverTimestamp()

                val messageMap = mutableMapOf(
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "senderType" to senderType,
                    "text" to text,
                    "timestamp" to serverTime,
                    "isCallMessage" to isCallMessage
                )
                audioUrl?.let { messageMap["audioUrl"] = it }
                imageUrl?.let { messageMap["imageUrl"] = it }
                videoUrl?.let { messageMap["videoUrl"] = it }
                glbUrl?.let { messageMap["glbUrl"] = it }
                fileUrl?.let { messageMap["fileUrl"] = it }
                fileName?.let { messageMap["fileName"] = it }

                firestore.runBatch { batch ->
                    batch.set(messageRef, messageMap)
                    if (!isCallMessage) {
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
                    }
                }.await()

                // Update local status to SENT
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.SENT)))

                // Trigger Notification only if not a call message
                if (!isCallMessage) {
                    try {
                        val participants = chatId.split("_")
                        val recipientId = participants.find { it != senderId } ?: return@launch

                        val recipientToken = authRepository.getFcmToken(recipientId)

                        if (!recipientToken.isNullOrBlank()) {
                            val (authHeader, projectId) = getAccessToken()
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
                            fcmApi.sendNotification(authHeader, request, projectId)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Failed to send notification: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Update status to FAILED if offline/error
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.FAILED)))
            }
        }
    }

    override suspend fun sendCallNotification(
        chatId: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String?
    ) {
        try {
            val chat = chatDao.getChatById(chatId)
            val recipients = if (chat != null && chat.id.startsWith("group_")) {
                chat.participants.filter { it != senderId }
            } else {
                chatId.split("_").filter { it != senderId }
            }

            if (recipients.isEmpty()) {
                Log.w("ChatRepository", "No recipients found for call notification in chat: $chatId")
                return
            }

            val groupName = if (chat != null && chat.id.startsWith("group_")) {
                chat.participantNames[chatId] ?: "Group"
            } else null

            val notificationTitle = groupName ?: "Incoming Video Call"
            val notificationBody = if (groupName != null) {
                "$senderName is starting a group call..."
            } else {
                "$senderName is calling you..."
            }

            for (recipientId in recipients) {
                // 1. Create the call invite document in Firestore
                val inviteId = "invite_${chatId}_${recipientId}"
                val inviteData = mutableMapOf(
                    "status" to "active",
                    "type" to "room_invite",
                    "roomId" to chatId,
                    "recipientId" to recipientId,
                    "callerId" to senderId,
                    "callerName" to (groupName ?: senderName),
                    "callerImageUrl" to senderImageUrl,
                    "createdAt" to Timestamp.now()
                )

                firestore.collection("calls").document(inviteId).set(inviteData).await()
                Log.d("ChatRepository", "Call invite created in Firestore: $inviteId for $recipientId")

                // 2. Send FCM Notification
                val recipientToken = authRepository.getFcmToken(recipientId)
                if (!recipientToken.isNullOrBlank()) {
                    try {
                        val (authHeader, projectId) = getAccessToken()
                        val request = FcmV1Request(
                            message = FcmMessage(
                                token = recipientToken,
                                notification = NotificationData(
                                    title = notificationTitle,
                                    body = notificationBody
                                ),
                                data = mapOf(
                                    "type" to "VIDEO_CALL",
                                    "chatId" to chatId,
                                    "roomId" to chatId,
                                    "inviteId" to inviteId,
                                    "senderId" to senderId,
                                    "senderName" to (groupName ?: senderName),
                                    "senderImageUrl" to (senderImageUrl ?: "")
                                ),
                                android = AndroidConfig(
                                    priority = "high",
                                    notification = AndroidNotification(
                                        channel_id = "call_notifications",
                                        notification_priority = "PRIORITY_HIGH"
                                    )
                                )
                            )
                        )
                        fcmApi.sendNotification(authHeader, request, projectId)
                        Log.d("ChatRepository", "Call notification sent via FCM to $recipientId")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Failed to send FCM to $recipientId: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to send call notification: ${e.message}")
        }
    }

    override suspend fun updateGroupSettings(
        chatId: String,
        name: String?,
        description: String?,
        imageUrl: String?,
        isOnlyMentorMessaging: Boolean?
    ) {
        val updates = mutableMapOf<String, Any>()
        name?.let { updates["participantNames.$chatId"] = it }
        description?.let { updates["groupDescription"] = it }
        imageUrl?.let { updates["participantProfileImages.$chatId"] = it }
        isOnlyMentorMessaging?.let { updates["onlyMentorMessaging"] = it }

        if (updates.isNotEmpty()) {
            firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
                .update(updates).await()

            // Local update
            chatDao.getChatById(chatId)?.let { chat ->
                val newNames =
                    chat.participantNames.toMutableMap().apply { name?.let { put(chatId, it) } }
                val newImages = chat.participantProfileImages.toMutableMap()
                    .apply { imageUrl?.let { put(chatId, it) } }

                chatDao.insertChats(
                    listOf(
                        chat.copy(
                            participantNames = newNames,
                            participantProfileImages = newImages,
                            groupDescription = description ?: chat.groupDescription,
                            isOnlyMentorMessaging = isOnlyMentorMessaging
                                ?: chat.isOnlyMentorMessaging
                        )
                    )
                )
            }
        }
    }

    override suspend fun leaveGroup(chatId: String, userId: String) {
        firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            .update(
                "participants", FieldValue.arrayRemove(userId),
                "participantNames.$userId", FieldValue.delete(),
                "participantProfileImages.$userId", FieldValue.delete()
            ).await()

        chatDao.getChatById(chatId)?.let { chat ->
            val newParticipants = chat.participants.filter { it != userId }
            if (newParticipants.isEmpty()) {
                // Optionally delete the chat if no participants left
                // For now just update local
            }
            chatDao.insertChats(listOf(chat.copy(participants = newParticipants)))
        }
    }

    override suspend fun restrictMember(chatId: String, userId: String) {
        firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            .update("restrictedParticipants", FieldValue.arrayUnion(userId)).await()

        chatDao.getChatById(chatId)?.let { chat ->
            val newList = chat.restrictedParticipants.toMutableList().apply { add(userId) }
            chatDao.insertChats(listOf(chat.copy(restrictedParticipants = newList)))
        }
    }

    override suspend fun unrestrictMember(chatId: String, userId: String) {
        firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
            .update("restrictedParticipants", FieldValue.arrayRemove(userId)).await()

        chatDao.getChatById(chatId)?.let { chat ->
            val newList = chat.restrictedParticipants.toMutableList().apply { remove(userId) }
            chatDao.insertChats(listOf(chat.copy(restrictedParticipants = newList)))
        }
    }

    override suspend fun sendPoll(chatId: String, question: String, options: List<String>) {
        val user = authRepository.getCurrentUser() ?: return
        val senderId = user.userId ?: ""
        val senderName = user.fullName ?: "Unknown"
        val senderType = user.accountType.name

        val messageId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val poll = Poll(question = question, options = options)

        val localMessage = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderType = senderType,
            text = "Poll: $question",
            poll = poll,
            timestamp = currentTime / 1000,
            status = MessageStatus.SENDING
        )

        messageDao.insertMessages(listOf(localMessage))

        repositoryScope.launch {
            try {
                val chatRef =
                    firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
                val messageRef =
                    chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document(messageId)
                val serverTime = FieldValue.serverTimestamp()

                val messageMap = mutableMapOf(
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "senderType" to senderType,
                    "text" to "Poll: $question",
                    "poll" to poll,
                    "timestamp" to serverTime
                )

                firestore.runBatch { batch ->
                    batch.set(messageRef, messageMap)
                    val chatUpdate = mapOf(
                        "lastMessage" to "Poll: $question",
                        "lastMessageTimestamp" to serverTime,
                        "lastSenderId" to senderId,
                        "lastSenderName" to senderName
                    )
                    batch.set(chatRef, chatUpdate, SetOptions.merge())
                }.await()

                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.SENT)))
            } catch (e: Exception) {
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.FAILED)))
            }
        }
    }

    override suspend fun voteOnPoll(
        chatId: String,
        messageId: String,
        optionIndex: Int,
        userId: String
    ) {
        val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
        val messageRef =
            chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document(messageId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(messageRef)
            val message = snapshot.toObject(Message::class.java) ?: return@runTransaction
            val poll = message.poll ?: return@runTransaction

            val newVotes = poll.votes.toMutableMap()

            // Remove user from any previous option they voted for
            newVotes.keys.forEach { idx ->
                val voters = newVotes[idx]?.toMutableList() ?: mutableListOf()
                if (voters.contains(userId)) {
                    voters.remove(userId)
                    newVotes[idx] = voters
                }
            }

            // Add user to the new option
            val targetVoters = newVotes[optionIndex.toString()]?.toMutableList() ?: mutableListOf()
            targetVoters.add(userId)
            newVotes[optionIndex.toString()] = targetVoters

            transaction.update(messageRef, "poll.votes", newVotes)
        }.await()
    }

    override suspend fun sendBookingRequest(
        chatId: String,
        batchId: String,
        sessionId: String,
        proposedTimes: List<Long>
    ) {
        val user = authRepository.getCurrentUser() ?: return
        val senderId = user.userId ?: ""
        val senderName = user.fullName ?: "Unknown"
        val senderType = user.accountType.name

        val messageId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val bookingRequest = BookingRequest(
            sessionId = sessionId,
            batchId = batchId,
            proposedTimes = proposedTimes,
            status = "PENDING"
        )

        val localMessage = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderType = senderType,
            text = "Booking Request for Session",
            bookingRequest = bookingRequest,
            timestamp = currentTime / 1000,
            status = MessageStatus.SENDING
        )

        messageDao.insertMessages(listOf(localMessage))

        repositoryScope.launch {
            try {
                val chatRef =
                    firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
                val messageRef =
                    chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document(messageId)
                val serverTime = FieldValue.serverTimestamp()

                val messageMap = mutableMapOf(
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "senderType" to senderType,
                    "text" to "Booking Request for Session",
                    "bookingRequest" to bookingRequest,
                    "timestamp" to serverTime
                )

                firestore.runBatch { batch ->
                    batch.set(messageRef, messageMap)
                    val chatUpdate = mapOf(
                        "lastMessage" to "New Booking Request",
                        "lastMessageTimestamp" to serverTime,
                        "lastSenderId" to senderId,
                        "lastSenderName" to senderName
                    )
                    batch.set(chatRef, chatUpdate, SetOptions.merge())
                }.await()

                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.SENT)))
            } catch (e: Exception) {
                messageDao.insertMessages(listOf(localMessage.copy(status = MessageStatus.FAILED)))
            }
        }
    }

    override fun getCallMessages(chatId: String): Flow<List<Message>> {
        // Start remote listener if not already started
        if (!messageListeners.containsKey("${chatId}_call")) {
            val listener = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .document(chatId)
                .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Error in call messages listener: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val messages = it.documents.mapNotNull { doc ->
                            doc.toObject(Message::class.java)?.copy(id = doc.id)
                        }
                        repositoryScope.launch {
                            messageDao.insertMessages(messages.map { m -> m.toEntity(chatId) })
                        }
                    }
                }
            messageListeners["${chatId}_call"] = listener
        }

        return messageDao.getCallMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun cleanupCallMessages(chatId: String) {
        try {
            val callMessages = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .document(chatId)
                .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
                .whereEqualTo("isCallMessage", true)
                .get().await()

            if (!callMessages.isEmpty) {
                firestore.runBatch { batch ->
                    callMessages.documents.forEach { batch.delete(it.reference) }
                }.await()
            }
            
            // Also cleanup local
            messageDao.deleteCallMessagesForChat(chatId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun respondToBookingRequest(
        chatId: String,
        messageId: String,
        status: String
    ) {
        try {
            val messageRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
                .document(chatId)
                .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
                .document(messageId)

            messageRef.update("bookingRequest.status", status).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getAccessToken(): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val stream = context.assets.open("service-account.json")
                val jsonString = stream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(jsonString)
                val projectId = jsonObject.getString("project_id")

                val credentials = GoogleCredentials.fromStream(jsonString.byteInputStream())
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                credentials.refreshIfExpired()
                Pair("Bearer ${credentials.accessToken.tokenValue}", projectId)
            } catch (e: Exception) {
                Log.e(
                    "ChatRepository", "Error getting access token: ${e.message}. " +
                            "Ensure 'service-account.json' is in assets folder."
                )
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
            glbUrl = this.glbUrl,
            fileUrl = this.fileUrl,
            fileName = this.fileName,
            poll = this.poll,
            bookingRequest = this.bookingRequest,
            timestamp = this.timestamp?.seconds ?: (System.currentTimeMillis() / 1000),
            status = this.status,
            isCallMessage = this.isCallMessage
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
            glbUrl = this.glbUrl,
            fileUrl = this.fileUrl,
            fileName = this.fileName,
            poll = this.poll,
            bookingRequest = this.bookingRequest,
            timestamp = if (this.timestamp != 0L) Timestamp(this.timestamp, 0) else null,
            status = this.status,
            isCallMessage = this.isCallMessage
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
            participantProfileImages = this.participantProfileImages,
            isSupportChat = this.isSupportChat,
            creatorId = this.creatorId,
            groupDescription = this.groupDescription,
            restrictedParticipants = this.restrictedParticipants,
            isOnlyMentorMessaging = this.isOnlyMentorMessaging
        )
    }

    private fun ChatEntity.toDomain(): Chat {
        return Chat(
            id = this.id,
            participants = this.participants,
            lastMessage = this.lastMessage,
            lastMessageTimestamp = if (this.lastMessageTimestamp != 0L) Timestamp(
                this.lastMessageTimestamp,
                0
            ) else null,
            lastSenderName = this.lastSenderName,
            lastSenderId = this.lastSenderId,
            participantNames = this.participantNames,
            participantProfileImages = this.participantProfileImages,
            isSupportChat = this.isSupportChat,
            creatorId = this.creatorId,
            groupDescription = this.groupDescription,
            restrictedParticipants = this.restrictedParticipants,
            isOnlyMentorMessaging = this.isOnlyMentorMessaging
        )
    }

    fun cleanup() {
        messageListeners.values.forEach { it.remove() }
        messageListeners.clear()
        chatsListener?.remove()
        chatsListener = null
    }
}


