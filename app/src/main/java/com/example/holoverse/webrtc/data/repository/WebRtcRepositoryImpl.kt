package com.example.holoverse.webrtc.data.repository

import com.example.holoverse.webrtc.data.datasource.WebRtcSessionManager
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
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
    override val connectionState: StateFlow<org.webrtc.PeerConnection.PeerConnectionState?> = sessionManager.connectionState
    override val isCallEnded: StateFlow<Boolean> = sessionManager.isCallEnded

    override fun init(callId: String, isOffer: Boolean) {
        sessionManager.init(callId, isOffer)
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
}
