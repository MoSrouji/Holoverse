package com.example.holoverse.chat_system.data.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.network.NetworkConstant
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message
import com.example.holoverse.chat_system.domain.repository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS)
            .document(chatId)
            .collection(NetworkConstant.COLLECTION_NAME_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100) // Optimization: Fetch only recent messages initially
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        senderType: String
    ) {
        val message = Message(
            senderId = senderId,
            senderName = senderName,
            senderType = senderType,
            text = text,
            timestamp = Timestamp.now()
        )

        val chatRef = firestore.collection(NetworkConstant.COLLECTION_NAME_CHATS).document(chatId)
        val messageRef = chatRef.collection(NetworkConstant.COLLECTION_NAME_MESSAGES).document()

        firestore.runBatch { batch ->
            // 1. Write the message
            batch.set(messageRef, message)

            // 2. Update Chat Metadata for efficient list viewing
            val chatUpdate = mapOf(
                "lastMessage" to text,
                "lastMessageTimestamp" to message.timestamp,
                "lastSenderId" to senderId,
                // Ensure participants exist (useful for first message)
                "participants" to com.google.firebase.firestore.FieldValue.arrayUnion(senderId)
            )
            batch.set(chatRef, chatUpdate, SetOptions.merge())
        }.await()
    }
}
