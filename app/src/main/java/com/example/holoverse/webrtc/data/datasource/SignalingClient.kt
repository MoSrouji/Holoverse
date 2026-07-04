package com.example.holoverse.webrtc.data.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
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
    private val currentUserId: String get() = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in for signaling")

    private val activeProcessedCalls = mutableSetOf<String>()

    fun sendOffer(callId: String, sdp: SessionDescription) {
        Log.d(TAG, "Sending offer for call: $callId")
        val participants = callId.split("_")
        val recipientId = participants.find { it != currentUserId } ?: "unknown"
        
        val data = mapOf<String, Any>(
            "status" to "active",
            "recipientId" to recipientId,
            "callerId" to currentUserId,
            "offer" to mapOf(
                "sdp" to sdp.description,
                "type" to sdp.type.canonicalForm(),
                "senderId" to currentUserId
            )
        )
        callsCollection.document(callId).set(data)
    }

    fun sendAnswer(callId: String, sdp: SessionDescription) {
        Log.d(TAG, "Sending answer for call: $callId")
        val data = mapOf<String, Any>(
            "answer" to mapOf(
                "sdp" to sdp.description,
                "type" to sdp.type.canonicalForm(),
                "senderId" to currentUserId
            )
        )
        callsCollection.document(callId).update(data)
    }

    fun sendIceCandidate(callId: String, candidate: IceCandidate, isOffer: Boolean) {
        val type = if (isOffer) "offerCandidates" else "answerCandidates"
        Log.d(TAG, "Sending ICE candidate ($type) for call: $callId")
        val data = hashMapOf(
            "sdp" to candidate.sdp,
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "senderId" to currentUserId
        )
        callsCollection.document(callId).collection(type).add(data)
    }

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
                Log.d(TAG, "Call document deleted or doesn't exist")
                trySend(SignalingEvent.CallEnded)
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

    fun observeCandidates(callId: String, isOffer: Boolean) = callbackFlow {
        val type = if (isOffer) "answerCandidates" else "offerCandidates"
        Log.d(TAG, "Observing ICE candidates ($type) for call: $callId")
        val subscription =
            callsCollection.document(callId).collection(type).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error observing candidates: $e")
                    close(e)
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val senderId = data["senderId"] as? String
                        val sdp = data["sdp"] as? String
                        val sdpMid = data["sdpMid"] as? String
                        val sdpMLineIndexObj = data["sdpMLineIndex"]
                        
                        val sdpMLineIndex = when (sdpMLineIndexObj) {
                            is Long -> sdpMLineIndexObj.toInt()
                            is Int -> sdpMLineIndexObj
                            else -> -1
                        }

                        if (senderId != null && senderId != currentUserId && sdp != null && sdpMid != null && sdpMLineIndex != -1) {
                            Log.d(TAG, "New ICE candidate received from Firestore ($type, sender: $senderId)")
                            trySend(IceCandidate(sdpMid, sdpMLineIndex, sdp))
                        } else if (senderId == currentUserId) {
                            Log.v(TAG, "Ignoring self-sent ICE candidate")
                        } else {
                            Log.w(TAG, "Received invalid ICE candidate: $data")
                        }
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
                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    val doc = change.document
                    val callId = doc.id
                    val hasAnswer = doc.contains("answer")
                    
                    if (!hasAnswer && !activeProcessedCalls.contains(callId)) {
                        Log.d(TAG, "Global observer: New unanswered call detected: $callId")
                        activeProcessedCalls.add(callId)
                        trySend(callId)
                    } else {
                        Log.d(TAG, "Global observer: Ignoring already processed or answered call: $callId")
                    }
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    fun markCallAsProcessed(callId: String) {
        activeProcessedCalls.add(callId)
    }

    suspend fun checkExistingOffer(callId: String): SessionDescription? {
        return try {
            val snapshot = callsCollection.document(callId).get().await()
            val offerMap = snapshot.get("offer") as? Map<String, Any>
            if (offerMap != null) {
                val sdp = offerMap["sdp"] as? String
                val type = offerMap["type"] as? String
                if (sdp != null && type != null) {
                    SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type),
                        sdp
                    )
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing offer: $e")
            null
        }
    }

    fun clearCall(callId: String) {
        Log.d(TAG, "clearCall: Triggered for $callId")
        val callDoc = callsCollection.document(callId)
        activeProcessedCalls.remove(callId)
        // Set status to ended first so the other peer knows
        callDoc.update("status", "ended").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "clearCall: Status updated to 'ended' for $callId")
            } else {
                Log.e(TAG, "clearCall: Failed to update status for $callId: ${task.exception}")
            }
            
            // Delete subcollections best-effort
            callDoc.collection("offerCandidates").get().addOnSuccessListener { snapshot ->
                Log.d(TAG, "clearCall: Deleting ${snapshot.size()} offer candidates")
                snapshot.documents.forEach { it.reference.delete() }
            }
            callDoc.collection("answerCandidates").get().addOnSuccessListener { snapshot ->
                Log.d(TAG, "clearCall: Deleting ${snapshot.size()} answer candidates")
                snapshot.documents.forEach { it.reference.delete() }
            }
            
            callDoc.delete().addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    Log.d(TAG, "clearCall: Call document $callId deleted successfully")
                } else {
                    Log.e(TAG, "clearCall: Failed to delete call document $callId: ${deleteTask.exception}")
                }
            }
        }
    }
}

sealed class SignalingEvent {
    data class OfferReceived(val offer: SessionDescription) : SignalingEvent()
    data class AnswerReceived(val answer: SessionDescription) : SignalingEvent()
    object CallEnded : SignalingEvent()
}
