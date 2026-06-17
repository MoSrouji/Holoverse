package com.example.holoverse.webrtc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val sessionManager: WebRtcSessionManager
) : ViewModel() {

    val localVideoTrack: StateFlow<VideoTrack?> = sessionManager.localVideoTrack
    val remoteVideoTrack: StateFlow<VideoTrack?> = sessionManager.remoteVideoTrack

    fun initCall(callId: String, isOffer: Boolean) {
        sessionManager.init(callId, isOffer)
        if (isOffer) {
            sessionManager.startCall(callId)
        }
    }

    fun endCall() {
        sessionManager.disconnect()
    }

    fun getEglContext() = sessionManager.getEglContext()

    override fun onCleared() {
        super.onCleared()
        sessionManager.disconnect()
    }
}
