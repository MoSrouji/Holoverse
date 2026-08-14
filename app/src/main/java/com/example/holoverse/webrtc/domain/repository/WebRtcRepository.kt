package com.example.holoverse.webrtc.domain.repository

import android.view.Surface
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.webrtc.domain.model.Participant
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import kotlinx.coroutines.flow.StateFlow
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.VideoTrack

interface WebRtcRepository {
    val localVideoTrack: StateFlow<io.livekit.android.room.track.VideoTrack?>
    val remoteVideoTrack: StateFlow<livekit.org.webrtc.VideoTrack?>
    val remoteParticipants: StateFlow<List<Participant>>
    val room: StateFlow<io.livekit.android.room.Room?> // Add this
    val connectionState: StateFlow<String?>
    val isCallEnded: StateFlow<Boolean>
    val isArEnabled: StateFlow<Boolean>
    val isWhiteboardEnabled: StateFlow<Boolean>
    val isPdfEnabled: StateFlow<Boolean>
    val isMessagingRestricted: StateFlow<Boolean>
    val callMode: StateFlow<CallMode>
    val arMirrorSurface: StateFlow<Surface?>
    val syntheticResolution: StateFlow<Pair<Int, Int>?>
    val pdfBitmap: StateFlow<android.graphics.Bitmap?>

    suspend fun joinRoom(url: String, token: String, roomId: String, inviteId: String? = null)
    suspend fun getJoinToken(roomName: String): String
    fun startSignalingObservation(callId: String)
    fun sendInvite(roomId: String, recipientId: String)
    suspend fun disconnect()
    fun getEglContext(): EglBase.Context
    fun toggleMute(muted: Boolean)
    fun toggleCamera(enabled: Boolean)
    fun toggleSpeaker(enabled: Boolean)
    fun switchCamera()
    fun toggleArMode(enabled: Boolean)
    fun toggleWhiteboardMode(enabled: Boolean)
    fun togglePdfMode(enabled: Boolean)
    fun setMessagingRestricted(restricted: Boolean)
    fun setCallMode(mode: CallMode)
    fun setWhiteboardManager(manager: WhiteboardManager?)
    fun loadPdf(uri: android.net.Uri)
    fun pdfNextPage()
    fun pdfPreviousPage()
}
