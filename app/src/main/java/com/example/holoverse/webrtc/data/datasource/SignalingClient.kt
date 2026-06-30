package com.example.holoverse.webrtc.data.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

class SignalingClient @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "SignalingClient"
    private val callsCollection = firestore.collection("calls")
    private val currentUserId: String get() = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in for signaling")

    fun sendOffer(callId: String, sdp: SessionDescription) {
        Log.d(TAG, "Sending offer for call: $callId")
        val data = mapOf<String, Any>(
            "status" to "active",
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
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                if (status == "ended") {
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
                        val sdpMLineIndex = data["sdpMLineIndex"] as? Long
                        if (senderId != null && senderId != currentUserId && sdp != null && sdpMid != null && sdpMLineIndex != null) {
                            Log.d(
                                TAG,
                                "New ICE candidate received from Firestore ($type, sender: $senderId)"
                            )
                            trySend(
                                IceCandidate(
                                    sdpMid,
                                    sdpMLineIndex.toInt(),
                                    sdp
                                )
                            )
                        } else {
                            Log.v(TAG, "Ignoring self-sent or invalid ICE candidate")
                        }
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    fun clearCall(callId: String) {
        val callDoc = callsCollection.document(callId)
        // Set status to ended first so the other peer knows
        callDoc.update("status", "ended")

        // Note: Firestore doesn't support recursive delete from client easily.
        // For production, a Cloud Function is better.
        // Here we do a best effort for the two known subcollections.
        callDoc.collection("offerCandidates").get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { it.reference.delete() }
        }
        callDoc.collection("answerCandidates").get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { it.reference.delete() }
        }
        callDoc.delete()
    }
}

sealed class SignalingEvent {
    data class OfferReceived(val offer: SessionDescription) : SignalingEvent()
    data class AnswerReceived(val answer: SessionDescription) : SignalingEvent()
    object CallEnded : SignalingEvent()
}
