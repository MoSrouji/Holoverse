package com.example.holoverse.webrtc.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import org.webrtc.VideoTrack

interface WebRtcRepository {
    val localVideoTrack: StateFlow<VideoTrack?>
    val remoteVideoTrack: StateFlow<VideoTrack?>
    val connectionState: StateFlow<org.webrtc.PeerConnection.PeerConnectionState?>
    val isCallEnded: StateFlow<Boolean>

    fun init(callId: String, isOffer: Boolean)
    fun startCall(callId: String)
    fun disconnect()
    fun getEglContext(): EglBase.Context
    fun toggleMute(muted: Boolean)
    fun toggleCamera(enabled: Boolean)
    fun toggleSpeaker(enabled: Boolean)
    fun switchCamera()
}
