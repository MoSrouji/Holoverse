package com.example.holoverse.webrtc.domain.usecase

import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import javax.inject.Inject

class InitCallUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(callId: String, isOffer: Boolean) = repository.init(callId, isOffer)
}

class StartCallUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(callId: String) = repository.startCall(callId)
}

class EndCallUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.disconnect()
}

class ObserveLocalVideoTrackUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.localVideoTrack
}

class ObserveRemoteVideoTrackUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.remoteVideoTrack
}

class ObserveIsCallEndedUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.isCallEnded
}

class GetEglContextUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.getEglContext()
}

class ObserveConnectionStateUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.connectionState
}

class ToggleMuteUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(muted: Boolean) = repository.toggleMute(muted)
}

class ToggleCameraUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(enabled: Boolean) = repository.toggleCamera(enabled)
}

class ToggleSpeakerUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(enabled: Boolean) = repository.toggleSpeaker(enabled)
}

class SwitchCameraUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.switchCamera()
}

class ToggleArModeUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(enabled: Boolean) = repository.toggleArMode(enabled)
}

class ToggleWhiteboardModeUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(enabled: Boolean) = repository.toggleWhiteboardMode(enabled)
}

class ObserveIsArEnabledUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.isArEnabled
}

class ObserveIsWhiteboardEnabledUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.isWhiteboardEnabled
}

class ObserveArMirrorSurfaceUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.arMirrorSurface
}
