package com.example.holoverse.webrtc.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioManager
import android.util.Log
import android.view.Surface
import com.example.holoverse.pdf.presentation.PdfManager
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class WebRtcSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signalingClient: SignalingClient
) {
    private val TAG = "WebRtcSessionManager"
    private val eglBaseContext: EglBase.Context = EglBase.create().eglBaseContext

    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _connectionState = MutableStateFlow<PeerConnection.PeerConnectionState?>(null)
    val connectionState: StateFlow<PeerConnection.PeerConnectionState?> = _connectionState

    private val pendingIceCandidates = mutableListOf<IceCandidate>()
    private var isRemoteDescriptionSet = false

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
    }

    private val peerConnectionFactory = PeerConnectionFactory.builder()
        .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
        .createPeerConnectionFactory()

    private var peerConnection: PeerConnection? = null
    private var videoCapturer: VideoCapturer? = null
    private var localVideoTrackInternal: VideoTrack? = null
    private var videoSender: org.webrtc.RtpSender? = null
    private var localAudioTrack: AudioTrack? = null
    private var localSurfaceTextureHelper: SurfaceTextureHelper? = null

    private var currentCallId: String? = null
    private var offerHandled = false
    private var isDisconnecting = false

    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack

    private val _isCallEnded = MutableStateFlow(false)
    val isCallEnded: StateFlow<Boolean> = _isCallEnded

    private val _callMode =
        MutableStateFlow(com.example.holoverse.webrtc.domain.model.CallMode.VIDEO)
    val callMode: StateFlow<com.example.holoverse.webrtc.domain.model.CallMode> = _callMode

    private val _isArEnabled = MutableStateFlow(false)
    val isArEnabled: StateFlow<Boolean> = _isArEnabled

    private val _isWhiteboardEnabled = MutableStateFlow(false)
    val isWhiteboardEnabled: StateFlow<Boolean> = _isWhiteboardEnabled

    private val _isPdfEnabled = MutableStateFlow(false)
    val isPdfEnabled: StateFlow<Boolean> = _isPdfEnabled

    private val _arMirrorSurface = MutableStateFlow<Surface?>(null)
    val arMirrorSurface: StateFlow<Surface?> = _arMirrorSurface

    val pdfBitmap: StateFlow<Bitmap?> by lazy { pdfManager.currentBitmap }

    private var syntheticSurfaceTextureHelper: SurfaceTextureHelper? = null
    private var syntheticSurface: Surface? = null
    private var videoSourceObserver: org.webrtc.CapturerObserver? = null
    private var syntheticLoopJob: Job? = null

    val pdfManager by lazy { PdfManager(context) }
    private var whiteboardManager: WhiteboardManager? = null

    fun setWhiteboardManager(manager: WhiteboardManager?) {
        whiteboardManager = manager
    }

    fun init(callId: String, isOffer: Boolean): Boolean {
        if (peerConnection != null) {
            Log.d(TAG, "Already initialized for a call, ignoring init")
            return false
        }
        isDisconnecting = false
        currentCallId = callId
        offerHandled = false
        signalingClient.markCallAsProcessed(callId)
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        setupPeerConnection(callId, isOffer)
        setupLocalAudio()
        setupLocalVideo()
        observeSignaling(callId, isOffer)
        return true
    }

    private fun setupLocalAudio() {
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("AudioTrack", audioSource)
        peerConnection?.addTrack(localAudioTrack)
    }

    private fun setupPeerConnection(callId: String, isOffer: Boolean) {
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer(),

                // Open Relay TURN — Standard UDP port 80
                PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer(),

                // Open Relay TURN — TCP fallback on port 443
                PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer(),

                // Open Relay TURNS (TLS) — for strict corporate firewalls with DPI
                PeerConnection.IceServer.builder("turns:openrelay.metered.ca:443")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer()
            )
        ).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                    Log.d(TAG, "onSignalingChange: $p0")
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                    Log.i(TAG, "onConnectionChange: $newState")
                    _connectionState.value = newState
                    if (newState == PeerConnection.PeerConnectionState.FAILED ||
                        newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                        newState == PeerConnection.PeerConnectionState.CLOSED
                    ) {
                        _remoteVideoTrack.value = null
                    }
                }

                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.i(TAG, "onIceConnectionChange: $newState")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.d(TAG, "onIceConnectionReceivingChange: $p0")
                }

                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                    Log.d(TAG, "onIceGatheringChange: $p0")
                }

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.i(TAG, "onIceCandidate generated: ${candidate.sdpMid} - ${candidate.sdp}")
                    // Callers (isOffer=true) send to offerCandidates. 
                    // Receivers (isOffer=false) send to answerCandidates.
                    signalingClient.sendIceCandidate(callId, candidate, isOffer)
                }

                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                override fun onAddStream(p0: MediaStream?) {}
                override fun onRemoveStream(p0: MediaStream?) {}

                override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
                override fun onRenegotiationNeeded() {}

                override fun onAddTrack(
                    receiver: org.webrtc.RtpReceiver?,
                    streams: Array<out MediaStream>?
                ) {
                    val track = receiver?.track()
                    if (track is VideoTrack) {
                        _remoteVideoTrack.value = track
                    }
                }
            }
        )
    }

    private fun setupLocalVideo() {
        videoCapturer = createVideoCapturer()
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoSourceObserver = videoSource.capturerObserver
        localSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer?.initialize(localSurfaceTextureHelper, context, videoSourceObserver)

        val metrics = context.resources.displayMetrics
        videoCapturer?.startCapture(metrics.widthPixels, metrics.heightPixels, 30)

        localVideoTrackInternal = peerConnectionFactory.createVideoTrack(
            "VideoTrack",
            videoSource
        )
        _localVideoTrack.value = localVideoTrackInternal

        videoSender = peerConnection?.addTrack(localVideoTrackInternal)
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        // Try to find the back camera first
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        // Fallback to front camera if back camera is not found
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    private fun observeSignaling(callId: String, isOffer: Boolean) {
        scope.launch {
            signalingClient.observeCall(callId).collectLatest { event ->
                when (event) {
                    is SignalingEvent.OfferReceived -> {
                        if (!isOffer) {
                            handleOffer(callId, event.offer)
                        }
                    }

                    is SignalingEvent.AnswerReceived -> {
                        if (isOffer) {
                            handleAnswer(event.answer)
                        }
                    }

                    SignalingEvent.CallEnded -> {
                        Log.d(TAG, "Call ended by remote peer")
                        _isCallEnded.value = true
                    }
                }
            }
        }

        scope.launch {
            signalingClient.observeCandidates(callId, isOffer).collectLatest { candidate ->
                if (isRemoteDescriptionSet) {
                    Log.d(TAG, "Adding ICE candidate immediately")
                    peerConnection?.addIceCandidate(candidate)
                } else {
                    Log.d(TAG, "Buffering ICE candidate")
                    synchronized(pendingIceCandidates) {
                        pendingIceCandidates.add(candidate)
                    }
                }
            }
        }
    }

    fun startCall(callId: String) {
        if (peerConnection?.signalingState() != PeerConnection.SignalingState.STABLE) {
            Log.w(TAG, "Cannot start call: signaling state is ${peerConnection?.signalingState()}")
            return
        }
        Log.d(TAG, "Starting call: $callId")
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                Log.d(TAG, "Offer created successfully")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local description set successfully (Offer)")
                        signalingClient.sendOffer(callId, description)
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "Failed to set local description: $p0")
                    }

                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "Failed to set local description (onSetFailure): $p0")
                    }
                }, description)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "Failed to create offer: $p0")
            }

            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    private fun handleOffer(callId: String, offer: SessionDescription) {
        if (offerHandled) {
            Log.v(TAG, "Offer already handled, ignoring duplicate")
            return
        }
        if (peerConnection?.signalingState() != PeerConnection.SignalingState.STABLE) {
            Log.w(
                TAG,
                "Ignoring offer because signaling state is ${peerConnection?.signalingState()}"
            )
            return
        }
        offerHandled = true
        Log.d(TAG, "Handling offer for call: $callId")
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully (Offer)")
                isRemoteDescriptionSet = true
                drainIceCandidates()
                createAnswer(callId)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "Failed to set remote description: $p0")
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "Failed to set remote description (onSetFailure): $p0")
            }
        }, offer)
    }

    private fun createAnswer(callId: String) {
        Log.d(TAG, "Creating answer for call: $callId")
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                Log.d(TAG, "Answer created successfully")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local description set successfully (Answer)")
                        signalingClient.sendAnswer(callId, description)
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "Failed to set local description (Answer): $p0")
                    }

                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "Failed to set local description (Answer onSetFailure): $p0")
                    }
                }, description)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "Failed to create answer: $p0")
            }

            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    private fun handleAnswer(answer: SessionDescription) {
        if (peerConnection?.signalingState() != PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
            Log.w(
                TAG,
                "Ignoring answer because signaling state is ${peerConnection?.signalingState()}"
            )
            return
        }
        Log.d(TAG, "Handling answer")
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully (Answer)")
                isRemoteDescriptionSet = true
                drainIceCandidates()
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "Failed to set remote description (Answer): $p0")
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "Failed to set remote description (Answer onSetFailure): $p0")
            }
        }, answer)
    }

    private fun drainIceCandidates() {
        val candidates = synchronized(pendingIceCandidates) {
            val copy = pendingIceCandidates.toList()
            pendingIceCandidates.clear()
            copy
        }
        Log.d(TAG, "Draining ${candidates.size} pending ICE candidates")
        candidates.forEach {
            peerConnection?.addIceCandidate(it)
        }
    }

    fun disconnect() {
        if (isDisconnecting) {
            Log.d(TAG, "disconnect: Already disconnecting, ignoring")
            return
        }
        isDisconnecting = true
        Log.d(TAG, "disconnect: Starting session teardown for call $currentCallId")

        currentCallId?.let { id ->
            Log.d(TAG, "disconnect: Requesting Firestore cleanup for $id")
            signalingClient.clearCall(id)
        }

        stopSyntheticMode()
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        localSurfaceTextureHelper?.dispose()
        localSurfaceTextureHelper = null
        peerConnection?.close()
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null
        _connectionState.value = null
        videoSender = null
        _isCallEnded.value = false
        isRemoteDescriptionSet = false
        offerHandled = false
        currentCallId = null
        synchronized(pendingIceCandidates) {
            pendingIceCandidates.clear()
        }
        scope.cancel()
        peerConnection = null
    }

    fun getEglContext() = eglBaseContext

    fun toggleMute(muted: Boolean) {
        localAudioTrack?.setEnabled(!muted)
    }

    fun toggleCamera(enabled: Boolean) {
        localVideoTrackInternal?.setEnabled(enabled)
    }

    fun toggleSpeaker(enabled: Boolean) {
        audioManager.mode =
            if (enabled) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = enabled
    }

    fun switchCamera() {
        (videoCapturer as? org.webrtc.CameraVideoCapturer)?.switchCamera(null)
    }

    fun setCallMode(mode: com.example.holoverse.webrtc.domain.model.CallMode) {
        if (mode == _callMode.value) return

        val oldMode = _callMode.value
        Log.d(TAG, "Switching call mode from $oldMode to $mode")

        scope.launch {
            // PHASE 1: Release Resources
            if (mode == com.example.holoverse.webrtc.domain.model.CallMode.AR) {
                if (oldMode == com.example.holoverse.webrtc.domain.model.CallMode.VIDEO) {
                    Log.d(TAG, "Stopping camera for AR transition")
                    videoCapturer?.stopCapture()
                    delay(500) // Critical: Wait for camera hardware release
                }
            } else if (oldMode == com.example.holoverse.webrtc.domain.model.CallMode.AR) {
                // Moving AWAY from AR: first trigger UI switch so ARCore can release camera
                Log.d(TAG, "Moving away from AR mode")
                _callMode.value = mode
                updateLegacyFlags(mode)
                delay(500) // Critical: Wait for ARCore session to fully close
            }

            // PHASE 2: Start new mode resources
            when (mode) {
                com.example.holoverse.webrtc.domain.model.CallMode.VIDEO -> {
                    stopSyntheticMode()
                    if (_callMode.value != mode) {
                        _callMode.value = mode
                        updateLegacyFlags(mode)
                    }
                    val metrics = context.resources.displayMetrics
                    videoCapturer?.startCapture(metrics.widthPixels, metrics.heightPixels, 30)
                }

                else -> {
                    if (oldMode == com.example.holoverse.webrtc.domain.model.CallMode.VIDEO && mode != com.example.holoverse.webrtc.domain.model.CallMode.AR) {
                        videoCapturer?.stopCapture()
                    }
                    if (_callMode.value != mode) {
                        _callMode.value = mode
                        updateLegacyFlags(mode)
                    }
                    startSyntheticMode()
                }
            }

            // Update AR surface reference
            _arMirrorSurface.value =
                if (mode == com.example.holoverse.webrtc.domain.model.CallMode.AR) syntheticSurface else null
        }
    }

    private fun updateLegacyFlags(mode: com.example.holoverse.webrtc.domain.model.CallMode) {
        _isArEnabled.value = mode == com.example.holoverse.webrtc.domain.model.CallMode.AR
        _isWhiteboardEnabled.value =
            mode == com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD
        _isPdfEnabled.value = mode == com.example.holoverse.webrtc.domain.model.CallMode.PDF
    }

    private fun applyTextOptimizations(enabled: Boolean) {
        Log.d(TAG, "applyTextOptimizations: enabled=$enabled")

        // Set degradation preference to MAINTAIN_RESOLUTION
        // This prevents the encoder from dropping resolution (blurring) when bandwidth is low,
        // instead it will drop frame rate which is better for static content like PDFs.
        videoSender?.let { sender ->
            try {
                val parameters = sender.parameters
                parameters.degradationPreference = if (enabled) {
                    org.webrtc.RtpParameters.DegradationPreference.MAINTAIN_RESOLUTION
                } else {
                    org.webrtc.RtpParameters.DegradationPreference.BALANCED
                }
                sender.parameters = parameters
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set RtpParameters", e)
            }
        }
    }

    private fun startSyntheticMode() {
        if (syntheticSurfaceTextureHelper != null) return

        Log.d(TAG, "Starting synthetic mode capture")

        // Apply optimizations for PDF/Whiteboard content
        val isTextMode =
            _callMode.value == com.example.holoverse.webrtc.domain.model.CallMode.PDF ||
                    _callMode.value == com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD
        applyTextOptimizations(isTextMode)

        syntheticSurfaceTextureHelper =
            SurfaceTextureHelper.create("SyntheticThread", eglBaseContext)

        val metrics = context.resources.displayMetrics
        syntheticSurfaceTextureHelper?.setTextureSize(metrics.widthPixels, metrics.heightPixels)
        syntheticSurface = Surface(syntheticSurfaceTextureHelper?.surfaceTexture)

        syntheticSurfaceTextureHelper?.startListening { frame ->
            videoSourceObserver?.onFrameCaptured(frame)
        }

        syntheticLoopJob = scope.launch(Dispatchers.Default) {
            val paint =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)

            while (_callMode.value != com.example.holoverse.webrtc.domain.model.CallMode.VIDEO) {
                when (_callMode.value) {
                    com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD,
                    com.example.holoverse.webrtc.domain.model.CallMode.PDF -> {
                        val canvas = syntheticSurface?.lockCanvas(null)
                        if (canvas != null) {
                            when (_callMode.value) {
                                com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD -> {
                                    whiteboardManager?.drawToNativeCanvas(canvas)
                                        ?: canvas.drawColor(Color.WHITE)
                                }

                                com.example.holoverse.webrtc.domain.model.CallMode.PDF -> {
                                    pdfManager.currentBitmap.value?.let { bitmap ->
                                        val bmpWidth = bitmap.width
                                        val bmpHeight = bitmap.height
                                        val canvasWidth = canvas.width
                                        val canvasHeight = canvas.height

                                        // Compute scale to fit within canvas
                                        val scaleX = canvasWidth.toFloat() / bmpWidth
                                        val scaleY = canvasHeight.toFloat() / bmpHeight
                                        val scale = min(scaleX, scaleY)

                                        val dstWidth = (bmpWidth * scale).toInt()
                                        val dstHeight = (bmpHeight * scale).toInt()
                                        val left = (canvasWidth - dstWidth) / 2
                                        val top = (canvasHeight - dstHeight) / 2

                                        // Clear background
                                        canvas.drawColor(Color.WHITE)

                                        // Draw scaled bitmap with high quality filter
                                        val destRect =
                                            Rect(left, top, left + dstWidth, top + dstHeight)
                                        canvas.drawBitmap(bitmap, null, destRect, paint)
                                    } ?: canvas.drawColor(Color.WHITE)
                                    whiteboardManager?.drawToNativeCanvas(
                                        canvas,
                                        clearBackground = false
                                    )
                                }

                                else -> {}
                            }
                            syntheticSurface?.unlockCanvasAndPost(canvas)
                        }
                    }

                    com.example.holoverse.webrtc.domain.model.CallMode.AR -> {
                        // AR renders via mirrorSwapChain; do not touch canvas
                    }

                    else -> {}
                }

                // For PDF and Whiteboard, lower FPS is acceptable and helps maintain quality
                val frameDelay =
                    if (_callMode.value == com.example.holoverse.webrtc.domain.model.CallMode.AR) 33 else 100
                delay(frameDelay.milliseconds)
            }
        }
    }

    private fun stopSyntheticMode() {
        Log.d(TAG, "Stopping synthetic mode capture")

        // Reset optimizations back to default
        applyTextOptimizations(false)

        syntheticLoopJob?.cancel()
        syntheticLoopJob = null

        syntheticSurfaceTextureHelper?.stopListening()
        syntheticSurfaceTextureHelper?.dispose()
        syntheticSurfaceTextureHelper = null

        syntheticSurface?.release()
        syntheticSurface = null
        _arMirrorSurface.value = null
    }

    fun toggleWhiteboardMode(enabled: Boolean) {
        setCallMode(if (enabled) com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD else com.example.holoverse.webrtc.domain.model.CallMode.VIDEO)
    }

    fun toggleArMode(enabled: Boolean) {
        setCallMode(if (enabled) com.example.holoverse.webrtc.domain.model.CallMode.AR else com.example.holoverse.webrtc.domain.model.CallMode.VIDEO)
    }

    fun togglePdfMode(enabled: Boolean) {
        setCallMode(if (enabled) com.example.holoverse.webrtc.domain.model.CallMode.PDF else com.example.holoverse.webrtc.domain.model.CallMode.VIDEO)
    }

    fun loadPdf(uri: android.net.Uri) {
        pdfManager.loadPdf(uri)
    }

    fun pdfNextPage() {
        pdfManager.nextPage()
    }

    fun pdfPreviousPage() {
        pdfManager.previousPage()
    }
}
