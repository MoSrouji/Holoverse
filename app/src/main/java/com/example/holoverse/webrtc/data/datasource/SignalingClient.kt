package com.example.holoverse.webrtc.data.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingClient @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "SignalingClient"
    private val callsCollection = firestore.collection("calls")
    private val roomsCollection = firestore.collection("rooms")
    private val currentUserId: String get() = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in for signaling")

    private val activeProcessedCalls = mutableSetOf<String>()

    fun observeCall(callId: String) = callbackFlow {
        Log.d(TAG, "Observing call: $callId")
        var lastOfferSdp: String? = null
        var lastAnswerSdp: String? = null

        val subscription = callsCollection.document(callId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error observing call: $e")
                close(e)
                return@addSnapshotListener
            }
            
            if (snapshot == null || !snapshot.exists()) {
                Log.d(TAG, "Call document does not exist yet or was deleted: $callId")
                // Only end call if it was previously active or if we're not expecting it to be created
                // For now, let's just log and not immediately end to avoid startup race
                return@addSnapshotListener
            }

            val status = snapshot.getString("status")
            if (status == "ended") {
                Log.d(TAG, "Call status set to ended")
                trySend(SignalingEvent.CallEnded)
                return@addSnapshotListener
            }

            val offerMap = snapshot.get("offer") as? Map<String, Any>
            val answerMap = snapshot.get("answer") as? Map<String, Any>

            if (offerMap != null) {
                val senderId = offerMap["senderId"] as? String
                val sdp = offerMap["sdp"] as? String
                val type = offerMap["type"] as? String
                if (senderId != null && senderId != currentUserId && sdp != null && type != null && sdp != lastOfferSdp) {
                    Log.d(TAG, "Offer received from Firestore (sender: $senderId)")
                    lastOfferSdp = sdp
                    trySend(
                        SignalingEvent.OfferReceived(
                            SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type),
                                sdp
                            )
                        )
                    )
                } else {
                    Log.v(TAG, "Ignoring self-sent or duplicate offer")
                }
            }
            if (answerMap != null) {
                val senderId = answerMap["senderId"] as? String
                val sdp = answerMap["sdp"] as? String
                val type = answerMap["type"] as? String
                if (senderId != null && senderId != currentUserId && sdp != null && type != null && sdp != lastAnswerSdp) {
                    Log.d(TAG, "Answer received from Firestore (sender: $senderId)")
                    lastAnswerSdp = sdp
                    trySend(
                        SignalingEvent.AnswerReceived(
                            SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type),
                                sdp
                            )
                        )
                    )
                } else {
                    Log.v(TAG, "Ignoring self-sent or duplicate answer")
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    fun observeGlobalCalls(userId: String) = callbackFlow {
        Log.d(TAG, "observeGlobalCalls: Listening for calls to $userId")
        val query = callsCollection
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("status", "active")

        val subscription = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error in global call listener: $e")
                return@addSnapshotListener
            }
            
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val docId = doc.id
                val roomId = doc.getString("roomId") ?: docId

                when (change.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                        if (!activeProcessedCalls.contains(docId)) {
                            Log.d(TAG, "Global observer: New call detected: $docId, joining room: $roomId")
                            activeProcessedCalls.add(docId)
                            val callerName = doc.getString("callerName")
                            val callerImageUrl = doc.getString("callerImageUrl")
                            trySend(
                                GlobalCallEvent.IncomingCall(
                                    roomId = roomId,
                                    inviteId = docId,
                                    callerName = callerName,
                                    callerImageUrl = callerImageUrl
                                )
                            )
                        }
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        Log.d(TAG, "Global observer: Call removed: $docId")
                        activeProcessedCalls.remove(docId)
                        trySend(GlobalCallEvent.CallCancelled(inviteId = docId))
                    }
                    else -> {}
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    fun markCallAsProcessed(callId: String) {
        activeProcessedCalls.add(callId)
    }

    suspend fun clearCall(callId: String) {
        Log.d(TAG, "clearCall: Triggered for $callId")
        val callDoc = callsCollection.document(callId)
        activeProcessedCalls.remove(callId)
        
        try {
            // Set status to ended first so the other peer knows
            callDoc.update("status", "ended").await()
            Log.d(TAG, "clearCall: Status updated to 'ended' for $callId")
            
            // Delete subcollections best-effort
            val offerCandidates = callDoc.collection("offerCandidates").get().await()
            Log.d(TAG, "clearCall: Deleting ${offerCandidates.size()} offer candidates")
            offerCandidates.documents.forEach { it.reference.delete() }
            
            val answerCandidates = callDoc.collection("answerCandidates").get().await()
            Log.d(TAG, "clearCall: Deleting ${answerCandidates.size()} answer candidates")
            answerCandidates.documents.forEach { it.reference.delete() }
            
            callDoc.delete().await()
            Log.d(TAG, "clearCall: Call document $callId deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "clearCall: Error cleaning up call document $callId: ${e.message}")
        }
    }

    // --- LiveKit Room Methods ---

    fun createRoom(roomId: String) {
        val data = mapOf(
            "status" to "active",
            "hostId" to currentUserId,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        roomsCollection.document(roomId).set(data)
    }

    fun sendRoomInvite(recipientId: String, roomId: String) {
        // We use the 'calls' collection for invites to reuse existing listeners
        val inviteId = "invite_${roomId}_${recipientId}"
        val data = mapOf(
            "status" to "active",
            "type" to "room_invite",
            "roomId" to roomId,
            "recipientId" to recipientId,
            "callerId" to currentUserId,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        callsCollection.document(inviteId).set(data)
    }
}

sealed class SignalingEvent {
    data class OfferReceived(val offer: SessionDescription) : SignalingEvent()
    data class AnswerReceived(val answer: SessionDescription) : SignalingEvent()
    object CallEnded : SignalingEvent()
}

sealed class GlobalCallEvent {
    data class IncomingCall(
        val roomId: String,
        val inviteId: String,
        val callerName: String? = null,
        val callerImageUrl: String? = null
    ) : GlobalCallEvent()
    data class CallCancelled(val inviteId: String) : GlobalCallEvent()
}
