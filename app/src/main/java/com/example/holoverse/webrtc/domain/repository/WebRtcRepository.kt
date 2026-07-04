package com.example.holoverse.webrtc.domain.repository

import android.view.Surface
import com.example.holoverse.webrtc.domain.model.CallMode
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import org.webrtc.VideoTrack

interface WebRtcRepository {
    val localVideoTrack: StateFlow<VideoTrack?>
    val remoteVideoTrack: StateFlow<VideoTrack?>
    val connectionState: StateFlow<org.webrtc.PeerConnection.PeerConnectionState?>
    val isCallEnded: StateFlow<Boolean>
    val isArEnabled: StateFlow<Boolean>
    val isWhiteboardEnabled: StateFlow<Boolean>
    val isPdfEnabled: StateFlow<Boolean>
    val callMode: StateFlow<CallMode>
    val arMirrorSurface: StateFlow<Surface?>
    val pdfBitmap: StateFlow<android.graphics.Bitmap?>

    fun init(callId: String, isOffer: Boolean): Boolean
    fun startCall(callId: String)
    fun disconnect()
    fun getEglContext(): EglBase.Context
    fun toggleMute(muted: Boolean)
    fun toggleCamera(enabled: Boolean)
    fun toggleSpeaker(enabled: Boolean)
    fun switchCamera()
    fun toggleArMode(enabled: Boolean)
    fun toggleWhiteboardMode(enabled: Boolean)
    fun togglePdfMode(enabled: Boolean)
    fun setCallMode(mode: CallMode)
    fun setWhiteboardManager(manager: com.example.holoverse.ui.whiteboard.WhiteboardManager?)
    fun loadPdf(uri: android.net.Uri)
    fun pdfNextPage()
    fun pdfPreviousPage()
}
