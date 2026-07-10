package com.example.holoverse.webrtc.presentation

import androidx.lifecycle.ViewModel
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.webrtc.domain.usecase.EndCallUseCase
import com.example.holoverse.webrtc.domain.usecase.GetEglContextUseCase
import com.example.holoverse.webrtc.domain.usecase.InitCallUseCase
import com.example.holoverse.webrtc.domain.usecase.LoadPdfUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveArMirrorSurfaceUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveCallModeUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveConnectionStateUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveIsArEnabledUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveIsCallEndedUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveIsPdfEnabledUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveIsWhiteboardEnabledUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveLocalVideoTrackUseCase
import com.example.holoverse.webrtc.domain.usecase.ObservePdfBitmapUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveRemoteVideoTrackUseCase
import com.example.holoverse.webrtc.domain.usecase.PdfNextPageUseCase
import com.example.holoverse.webrtc.domain.usecase.PdfPreviousPageUseCase
import com.example.holoverse.webrtc.domain.usecase.SetCallModeUseCase
import com.example.holoverse.webrtc.domain.usecase.SetWhiteboardManagerUseCase
import com.example.holoverse.webrtc.domain.usecase.StartCallUseCase
import com.example.holoverse.webrtc.domain.usecase.SwitchCameraUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleArModeUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleCameraUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleMuteUseCase
import com.example.holoverse.webrtc.domain.usecase.TogglePdfModeUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleSpeakerUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleWhiteboardModeUseCase
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val initCallUseCase: InitCallUseCase,
    private val startCallUseCase: StartCallUseCase,
    private val endCallUseCase: EndCallUseCase,
    private val observeLocalVideoTrackUseCase: ObserveLocalVideoTrackUseCase,
    private val observeRemoteVideoTrackUseCase: ObserveRemoteVideoTrackUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeIsCallEndedUseCase: ObserveIsCallEndedUseCase,
    private val toggleMuteUseCase: ToggleMuteUseCase,
    private val toggleCameraUseCase: ToggleCameraUseCase,
    private val toggleSpeakerUseCase: ToggleSpeakerUseCase,
    private val switchCameraUseCase: SwitchCameraUseCase,
    private val toggleArModeUseCase: ToggleArModeUseCase,
    private val toggleWhiteboardModeUseCase: ToggleWhiteboardModeUseCase,
    private val togglePdfModeUseCase: TogglePdfModeUseCase,
    private val setCallModeUseCase: SetCallModeUseCase,
    private val setWhiteboardManagerUseCase: SetWhiteboardManagerUseCase,
    private val loadPdfUseCase: LoadPdfUseCase,
    private val pdfNextPageUseCase: PdfNextPageUseCase,
    private val pdfPreviousPageUseCase: PdfPreviousPageUseCase,
    private val observeIsArEnabledUseCase: ObserveIsArEnabledUseCase,
    private val observeIsWhiteboardEnabledUseCase: ObserveIsWhiteboardEnabledUseCase,
    private val observeIsPdfEnabledUseCase: ObserveIsPdfEnabledUseCase,
    private val observeCallModeUseCase: ObserveCallModeUseCase,
    private val observePdfBitmapUseCase: ObservePdfBitmapUseCase,
    private val observeArMirrorSurfaceUseCase: ObserveArMirrorSurfaceUseCase,
    private val getEglContextUseCase: GetEglContextUseCase
) : ViewModel() {

    val localVideoTrack: StateFlow<VideoTrack?> = observeLocalVideoTrackUseCase()
    val remoteVideoTrack: StateFlow<VideoTrack?> = observeRemoteVideoTrackUseCase()
    val connectionState = observeConnectionStateUseCase()
    val isCallEnded = observeIsCallEndedUseCase()
    val isArEnabled = observeIsArEnabledUseCase()
    val isWhiteboardEnabled = observeIsWhiteboardEnabledUseCase()
    val isPdfEnabled = observeIsPdfEnabledUseCase()
    val callMode = observeCallModeUseCase()
    val pdfBitmap = observePdfBitmapUseCase()
    val arMirrorSurface = observeArMirrorSurfaceUseCase()

    fun initCall(callId: String, isOffer: Boolean) {
        val initialized = initCallUseCase(callId, isOffer)
        if (initialized && isOffer) {
            startCallUseCase(callId)
        }
    }

    fun endCall() {
        endCallUseCase()
    }

    fun toggleMute(muted: Boolean) {
        toggleMuteUseCase(muted)
    }

    fun toggleCamera(enabled: Boolean) {
        toggleCameraUseCase(enabled)
    }

    fun toggleSpeaker(enabled: Boolean) {
        toggleSpeakerUseCase(enabled)
    }

    fun switchCamera() {
        switchCameraUseCase()
    }

    fun toggleArMode(enabled: Boolean) {
        toggleArModeUseCase(enabled)
    }

    fun toggleWhiteboardMode(enabled: Boolean) {
        toggleWhiteboardModeUseCase(enabled)
    }

    fun togglePdfMode(enabled: Boolean) {
        togglePdfModeUseCase(enabled)
    }

    fun setCallMode(mode: CallMode) {
        setCallModeUseCase(mode)
    }

    fun setWhiteboardManager(manager: WhiteboardManager?) {
        setWhiteboardManagerUseCase(manager)
    }

    fun loadPdf(uri: android.net.Uri) {
        loadPdfUseCase(uri)
    }

    fun pdfNextPage() {
        pdfNextPageUseCase()
    }

    fun pdfPreviousPage() {
        pdfPreviousPageUseCase()
    }

    fun getEglContext(): EglBase.Context = getEglContextUseCase()

}
