package com.example.holoverse.webrtc.data.repository

import android.view.Surface
import com.example.holoverse.webrtc.data.datasource.LiveKitSessionManager
import com.example.holoverse.webrtc.data.remote.LiveKitTokenApi
import com.example.holoverse.webrtc.data.remote.TokenRequest
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.webrtc.domain.model.Participant
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcRepositoryImpl @Inject constructor(
    private val sessionManager: LiveKitSessionManager,
    private val tokenApi: LiveKitTokenApi,
    private val auth: FirebaseAuth
) : WebRtcRepository {

    override val localVideoTrack: StateFlow<io.livekit.android.room.track.VideoTrack?> = sessionManager.localVideoTrack
    override val remoteVideoTrack: StateFlow<livekit.org.webrtc.VideoTrack?> = MutableStateFlow(null)
    override val remoteParticipants: StateFlow<List<Participant>> = sessionManager.remoteParticipants
    override val room: StateFlow<io.livekit.android.room.Room?> = sessionManager.room
    override val connectionState: StateFlow<String?> = sessionManager.connectionStateString
    
    override val isCallEnded: StateFlow<Boolean> = sessionManager.isCallEnded
    override val isArEnabled: StateFlow<Boolean> = sessionManager.isArEnabled
    override val isWhiteboardEnabled: StateFlow<Boolean> = sessionManager.isWhiteboardEnabled
    override val isPdfEnabled: StateFlow<Boolean> = sessionManager.isPdfEnabled
    override val isMessagingRestricted: StateFlow<Boolean> = sessionManager.isMessagingRestricted
    override val callMode: StateFlow<CallMode> = sessionManager.callMode
    override val arMirrorSurface: StateFlow<Surface?> = sessionManager.arMirrorSurface
    override val syntheticResolution: StateFlow<Pair<Int, Int>?> = sessionManager.syntheticResolution
    override val pdfBitmap: StateFlow<android.graphics.Bitmap?> = sessionManager.pdfBitmap

    override suspend fun joinRoom(url: String, token: String, roomId: String, inviteId: String?) {
        sessionManager.joinRoom(url, token, roomId, inviteId)
    }

    override suspend fun getJoinToken(roomName: String): String {
        val currentUserId = auth.currentUser?.uid ?: "anonymous"
        android.util.Log.d("WebRtcRepositoryImpl", "Requesting token for room: $roomName, user: $currentUserId")
        return try {
            val response = tokenApi.getToken(
                TokenRequest(
                    roomName = roomName,
                    participantName = currentUserId
                )
            )
            val token = response.token
            if (token.isNotEmpty()) {
                android.util.Log.d("WebRtcRepositoryImpl", "Successfully received token (starts with: ${token.take(30)}...)")
            } else {
                android.util.Log.w("WebRtcRepositoryImpl", "Received empty token from server")
            }
            token
        } catch (e: Exception) {
            android.util.Log.e("WebRtcRepositoryImpl", "Failed to get LiveKit token from Cloudflare", e)
            ""
        }
    }

    override fun startSignalingObservation(callId: String) {
        sessionManager.startSignalingObservation(callId)
    }

    override fun sendInvite(roomId: String, recipientId: String) {
        sessionManager.sendInvite(roomId, recipientId)
    }

    override suspend fun disconnect() {
        sessionManager.disconnect()
    }

    override fun getEglContext(): EglBase.Context {
        return sessionManager.getEglContext()
    }

    override fun toggleMute(muted: Boolean) {
        sessionManager.toggleMute(muted)
    }

    override fun toggleCamera(enabled: Boolean) {
        sessionManager.toggleCamera(enabled)
    }

    override fun toggleSpeaker(enabled: Boolean) {
        sessionManager.toggleSpeaker(enabled)
    }

    override fun switchCamera() {
        sessionManager.switchCamera()
    }

    override fun toggleArMode(enabled: Boolean) {
        sessionManager.toggleArMode(enabled)
    }

    override fun toggleWhiteboardMode(enabled: Boolean) {
        sessionManager.toggleWhiteboardMode(enabled)
    }

    override fun togglePdfMode(enabled: Boolean) {
        sessionManager.togglePdfMode(enabled)
    }

    override fun setMessagingRestricted(restricted: Boolean) {
        sessionManager.setMessagingRestricted(restricted)
    }

    override fun setCallMode(mode: CallMode) {
        sessionManager.setCallMode(mode)
    }

    override fun setWhiteboardManager(manager: WhiteboardManager?) {
        sessionManager.setWhiteboardManager(manager)
    }

    override fun loadPdf(uri: android.net.Uri) {
        sessionManager.loadPdf(uri)
    }

    override fun pdfNextPage() {
        sessionManager.pdfNextPage()
    }

    override fun pdfPreviousPage() {
        sessionManager.pdfPreviousPage()
    }
}
