package com.example.holoverse.webrtc.domain.usecase

import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import javax.inject.Inject

class SendInviteUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(roomId: String, recipientId: String) = repository.sendInvite(roomId, recipientId)
}

class EndCallUseCase @Inject constructor(private val repository: WebRtcRepository) {
    suspend operator fun invoke() = repository.disconnect()
}

class ObserveLocalVideoTrackUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.StateFlow<io.livekit.android.room.track.VideoTrack?> = repository.localVideoTrack
}

class ObserveRemoteVideoTrackUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.StateFlow<livekit.org.webrtc.VideoTrack?> = repository.remoteVideoTrack
}

class ObserveRemoteParticipantsUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.remoteParticipants
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

class ObserveIsPdfEnabledUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.isPdfEnabled
}

class ObserveCallModeUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.callMode
}

class TogglePdfModeUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(enabled: Boolean) = repository.togglePdfMode(enabled)
}

class SetCallModeUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(mode: com.example.holoverse.webrtc.domain.model.CallMode) =
        repository.setCallMode(mode)
}

class SetWhiteboardManagerUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(manager: WhiteboardManager?) =
        repository.setWhiteboardManager(manager)
}

class LoadPdfUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(uri: android.net.Uri) = repository.loadPdf(uri)
}

class PdfNextPageUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.pdfNextPage()
}

class PdfPreviousPageUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.pdfPreviousPage()
}

class ObserveArMirrorSurfaceUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.arMirrorSurface
}

class ObservePdfBitmapUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.pdfBitmap
}

class ObserveSyntheticResolutionUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke() = repository.syntheticResolution
}
