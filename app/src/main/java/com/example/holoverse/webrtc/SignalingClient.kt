package com.example.holoverse.webrtc

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

import com.google.firebase.auth.FirebaseAuth

class SignalingClient @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "SignalingClient"
    private val callsCollection = firestore.collection("calls")
    private val currentUserId: String? get() = auth.currentUser?.uid

    fun sendOffer(callId: String, sdp: SessionDescription) {
        Log.d(TAG, "Sending offer for call: $callId")
        val data = mapOf<String, Any>(
            "offer" to mapOf(
                "sdp" to sdp.description,
                "type" to sdp.type.canonicalForm(),
                "senderId" to (currentUserId ?: "")
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
                "senderId" to (currentUserId ?: "")
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
            "senderId" to (currentUserId ?: "")
        )
        callsCollection.document(callId).collection(type).add(data)
    }

    fun observeCall(callId: String) = callbackFlow {
        Log.d(TAG, "Observing call: $callId")
        val subscription = callsCollection.document(callId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error observing call: $e")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val offerMap = snapshot.get("offer") as? Map<String, Any>
                val answerMap = snapshot.get("answer") as? Map<String, Any>
                
                if (offerMap != null) {
                    val senderId = offerMap["senderId"] as? String
                    if (senderId != currentUserId) {
                        Log.d(TAG, "Offer received from Firestore (sender: $senderId)")
                        trySend(SignalingEvent.OfferReceived(
                            SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(offerMap["type"] as String),
                                offerMap["sdp"] as String
                            )
                        ))
                    } else {
                        Log.v(TAG, "Ignoring self-sent offer")
                    }
                }
                if (answerMap != null) {
                    val senderId = answerMap["senderId"] as? String
                    if (senderId != currentUserId) {
                        Log.d(TAG, "Answer received from Firestore (sender: $senderId)")
                        trySend(SignalingEvent.AnswerReceived(
                            SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(answerMap["type"] as String),
                                answerMap["sdp"] as String
                            )
                        ))
                    } else {
                        Log.v(TAG, "Ignoring self-sent answer")
                    }
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    fun observeCandidates(callId: String, isOffer: Boolean) = callbackFlow {
        val type = if (isOffer) "answerCandidates" else "offerCandidates"
        Log.d(TAG, "Observing ICE candidates ($type) for call: $callId")
        val subscription = callsCollection.document(callId).collection(type).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error observing candidates: $e")
                return@addSnapshotListener
            }
            snapshot?.documentChanges?.forEach { change ->
                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    val data = change.document.data
                    val senderId = data["senderId"] as? String
                    if (senderId != currentUserId) {
                        Log.d(TAG, "New ICE candidate received from Firestore ($type, sender: $senderId)")
                        trySend(IceCandidate(
                            data["sdpMid"] as String,
                            (data["sdpMLineIndex"] as Long).toInt(),
                            data["sdp"] as String
                        ))
                    } else {
                        Log.v(TAG, "Ignoring self-sent ICE candidate")
                    }
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    fun clearCall(callId: String) {
        callsCollection.document(callId).delete()
    }
}

sealed class SignalingEvent {
    data class OfferReceived(val offer: SessionDescription) : SignalingEvent()
    data class AnswerReceived(val answer: SessionDescription) : SignalingEvent()
}
