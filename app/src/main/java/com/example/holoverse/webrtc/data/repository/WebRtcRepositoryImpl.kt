package com.example.holoverse.webrtc.data.repository

import android.view.Surface
import com.example.holoverse.webrtc.data.datasource.WebRtcSessionManager
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcRepositoryImpl @Inject constructor(
    private val sessionManager: WebRtcSessionManager
) : WebRtcRepository {

    override val localVideoTrack: StateFlow<VideoTrack?> = sessionManager.localVideoTrack
    override val remoteVideoTrack: StateFlow<VideoTrack?> = sessionManager.remoteVideoTrack
    override val connectionState: StateFlow<org.webrtc.PeerConnection.PeerConnectionState?> =
        sessionManager.connectionState
    override val isCallEnded: StateFlow<Boolean> = sessionManager.isCallEnded
    override val isArEnabled: StateFlow<Boolean> = sessionManager.isArEnabled
    override val isWhiteboardEnabled: StateFlow<Boolean> = sessionManager.isWhiteboardEnabled
    override val isPdfEnabled: StateFlow<Boolean> = sessionManager.isPdfEnabled
    override val callMode: StateFlow<CallMode> = sessionManager.callMode
    override val arMirrorSurface: StateFlow<Surface?> = sessionManager.arMirrorSurface
    override val pdfBitmap: StateFlow<android.graphics.Bitmap?> = sessionManager.pdfBitmap

    override fun init(callId: String, isOffer: Boolean): Boolean {
        return sessionManager.init(callId, isOffer)
    }

    override fun startCall(callId: String) {
        sessionManager.startCall(callId)
    }

    override fun disconnect() {
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
