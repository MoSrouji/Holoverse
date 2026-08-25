package com.example.holoverse.webrtc.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioManager
import android.util.Log
import android.view.Surface
import com.example.holoverse.pdf.presentation.PdfManager
import com.example.holoverse.webrtc.domain.model.Participant
import com.example.holoverse.webrtc.presentation.WebRtcCallService
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.VideoCaptureParameter
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import livekit.org.webrtc.CapturerObserver
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.SurfaceTextureHelper
import livekit.org.webrtc.VideoCapturer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class LiveKitSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signalingClient: SignalingClient
) {
    private val TAG = "LiveKitSessionManager"
    private val eglBase = EglBase.create()
    private val eglBaseContext: EglBase.Context = eglBase.eglBaseContext

    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room

    private val _connectionState = MutableStateFlow<Room.State>(Room.State.DISCONNECTED)
    val connectionState: StateFlow<Room.State> = _connectionState
    val connectionStateString: StateFlow<String?> = MutableStateFlow("DISCONNECTED")

    private val _remoteParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val remoteParticipants: StateFlow<List<Participant>> = _remoteParticipants

    private var currentRoomId: String? = null
    private var currentInviteId: String? = null
    private var isDisconnecting = false
    private var signalingJob: Job? = null

    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack

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

    private val _isMessagingRestricted = MutableStateFlow(false)
    val isMessagingRestricted: StateFlow<Boolean> = _isMessagingRestricted

    private val _arMirrorSurface = MutableStateFlow<Surface?>(null)
    val arMirrorSurface: StateFlow<Surface?> = _arMirrorSurface

    private val _syntheticResolution = MutableStateFlow<Pair<Int, Int>?>(null)
    val syntheticResolution: StateFlow<Pair<Int, Int>?> = _syntheticResolution

    val pdfBitmap: StateFlow<Bitmap?> by lazy { pdfManager.currentBitmap }

    private var syntheticSurfaceTextureHelper: SurfaceTextureHelper? = null
    private var syntheticSurface: Surface? = null
    private var syntheticCapturer: SyntheticCapturer? = null
    private var syntheticTrack: LocalVideoTrack? = null
    private var syntheticLoopJob: Job? = null

    val pdfManager by lazy { PdfManager(context) }
    private var whiteboardManager: WhiteboardManager? = null

    fun setWhiteboardManager(manager: WhiteboardManager?) {
        whiteboardManager = manager
    }

    fun sendInvite(roomId: String, recipientId: String) {
        signalingClient.sendRoomInvite(recipientId, roomId)
    }

    suspend fun joinRoom(url: String, token: String, roomId: String, inviteId: String? = null) {
        Log.d(TAG, "joinRoom: url=$url, roomId=$roomId, inviteId=$inviteId")

        // Always disconnect before a new join attempt to ensure hardware state is reset
        disconnect()

        // Set audio mode to communication for VoIP AFTER disconnect
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Start Foreground Service to ensure hardware priority on Android 14+
        WebRtcCallService.start(context)

        isDisconnecting = false
        currentRoomId = roomId
        currentInviteId = inviteId
        _isCallEnded.value = false

        // Ensure signaling observation is running for this call
        startSignalingObservation(inviteId ?: roomId)

        val newScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        scope = newScope

        Log.d(TAG, "Creating new LiveKit room instance with shared EGL context")
        val room = LiveKit.create(
            appContext = context,
            options = RoomOptions(),
            overrides = LiveKitOverrides(eglBase = eglBase)
        )
        _room.value = room

        setupRoomListeners(room, newScope)

        try {
            Log.d(TAG, "Connecting to LiveKit: url=$url")
            room.connect(url, token, options = ConnectOptions())
            Log.d(TAG, "LiveKit connected successfully. State: ${room.state}")

            _connectionState.value = room.state

            // Wait for connection to stabilize, then enable hardware
            newScope.launch {
                try {
                    Log.d(TAG, "Waiting 2s before enabling camera and microphone...")
                    delay(2000.milliseconds) // Increased delay for Android 14+ stability
                    Log.d(TAG, "Enabling microphone...")
                    room.localParticipant.setMicrophoneEnabled(true)
                    Log.d(TAG, "Enabling camera...")
                    room.localParticipant.setCameraEnabled(true)
                    Log.d(TAG, "Hardware enabled successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to enable hardware after connection", e)
                }
            }

            // Sync participants immediately after connection
            updateParticipants(room)

            // Sync local track
            updateLocalTrack(room)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to connect to LiveKit room. Type: ${e.javaClass.simpleName}, Message: ${e.message}",
                e
            )
            if (e is io.livekit.android.room.RoomException.ConnectException) {
                Log.e(TAG, "LiveKit Connection Error Detail: ${e.message}")
            }
            if (e !is kotlinx.coroutines.CancellationException) {
                _isCallEnded.value = true
            }
            throw e
        }
    }

    fun startSignalingObservation(callId: String) {
        if (signalingJob?.isActive == true && currentInviteId == callId) {
            Log.d(TAG, "Signaling observation already active for $callId")
            return
        }

        signalingJob?.cancel()
        currentInviteId = callId
        signalingJob = CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            Log.d(TAG, "observeSignaling: Starting to observe callId: $callId")
            signalingClient.observeCall(callId).collect { event ->
                Log.d(TAG, "observeSignaling: Received event $event for callId: $callId")
                if (event is SignalingEvent.CallEnded) {
                    Log.d(TAG, "Call ended signal received from Firestore for $callId")
                    _isCallEnded.value = true
                    disconnect()
                }
            }
        }
    }

    private fun setupRoomListeners(room: Room, scope: CoroutineScope) {
        scope.launch {
            room.events.collect { event ->
                Log.d(TAG, "Room Event: ${event.javaClass.simpleName}")
                _connectionState.value = room.state
                (connectionStateString as MutableStateFlow).value = room.state.name
                when (event) {
                    is RoomEvent.ParticipantConnected,
                    is RoomEvent.ParticipantDisconnected,
                    is RoomEvent.TrackPublished,
                    is RoomEvent.TrackUnpublished,
                    is RoomEvent.TrackSubscribed,
                    is RoomEvent.TrackUnsubscribed,
                    is RoomEvent.TrackMuted,
                    is RoomEvent.TrackUnmuted -> {
                        Log.d(TAG, "Updating participants due to event: $event")
                        updateParticipants(room)
                        updateLocalTrack(room)
                    }

                    is RoomEvent.DataReceived -> {
                        val payload = event.data.decodeToString()
                        Log.d(TAG, "Data received: $payload")
                        if (payload.startsWith("MODE:")) {
                            val modeName = payload.removePrefix("MODE:")
                            Log.d(TAG, "Remote participant switched to mode: $modeName")
                            // We no longer switch local UI mode based on remote data
                        } else if (payload.startsWith("CHAT_RESTRICTED:")) {
                            val restricted = payload.removePrefix("CHAT_RESTRICTED:").toBoolean()
                            Log.d(TAG, "Messaging restriction changed: $restricted")
                            _isMessagingRestricted.value = restricted
                        }
                    }

                    is RoomEvent.Disconnected -> {
                        Log.d(TAG, "Room disconnected")
                        _isCallEnded.value = true
                        disconnect()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateParticipants(room: Room) {
        val participants = room.remoteParticipants.values.map { rp ->
            // Prioritize 'synthetic' video track if available, otherwise take the first video track
            val lkTrack = rp.trackPublications.values
                .sortedByDescending { it.name == "synthetic" }
                .mapNotNull { it.track as? VideoTrack }
                .firstOrNull()

            Log.d(
                TAG,
                "Participant ${rp.identity?.value}: hasTrack=${lkTrack != null} isSynthetic=${lkTrack?.name == "synthetic"}"
            )
            Participant(
                id = rp.sid.value,
                identity = rp.identity?.value ?: "Unknown",
                videoTrack = null,
                livekitVideoTrack = lkTrack,
                isAudioEnabled = rp.isMicrophoneEnabled,
                isVideoEnabled = rp.isCameraEnabled || lkTrack != null
            )
        }
        _remoteParticipants.value = participants
    }

    private fun updateLocalTrack(room: Room) {
        // Prioritize local synthetic track if available
        val track = room.localParticipant.trackPublications.values
            .sortedByDescending { it.name == "synthetic" }
            .mapNotNull { it.track as? VideoTrack }
            .firstOrNull()

        if (track != null && _localVideoTrack.value != track) {
            Log.d(TAG, "Local video track updated: name=${track.name}")
            _localVideoTrack.value = track
        }
    }

    suspend fun disconnect() {
        if (isDisconnecting) return
        isDisconnecting = true

        stopSyntheticMode()
        
        // Stop the foreground service
        WebRtcCallService.stop(context)

        val inviteId = currentInviteId
        val roomId = currentRoomId
        val roomToClose = _room.value
        val disconnectScope = scope

        signalingJob?.cancel()
        signalingJob = null

        try {
            if (inviteId != null) {
                signalingClient.clearCall(inviteId)
            } else if (roomId != null) {
                signalingClient.clearCall(roomId)
            }

            roomToClose?.disconnect()

            if (_room.value === roomToClose) {
                _room.value = null
                _localVideoTrack.value = null
                _remoteParticipants.value = emptyList()
                _connectionState.value = Room.State.DISCONNECTED
                currentRoomId = null
                currentInviteId = null
                disconnectScope.cancel()
            }
        } finally {
            isDisconnecting = false
        }
    }

    fun toggleMute(muted: Boolean) {
        scope.launch {
            _room.value?.localParticipant?.setMicrophoneEnabled(!muted)
        }
    }

    fun toggleCamera(enabled: Boolean) {
        scope.launch {
            _room.value?.localParticipant?.setCameraEnabled(enabled)
        }
    }

    fun toggleSpeaker(enabled: Boolean) {
        audioManager.mode =
            if (enabled) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = enabled
    }

    fun switchCamera() {
        val track = _localVideoTrack.value as? LocalVideoTrack ?: return
        track.switchCamera()
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

    fun setMessagingRestricted(restricted: Boolean) {
        _isMessagingRestricted.value = restricted
        scope.launch {
            try {
                val message = "CHAT_RESTRICTED:$restricted".toByteArray()
                _room.value?.localParticipant?.publishData(message, DataPublishReliability.RELIABLE)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast messaging restriction", e)
            }
        }
    }

    fun setCallMode(
        mode: com.example.holoverse.webrtc.domain.model.CallMode,
        broadcast: Boolean = true
    ) {
        if (mode == _callMode.value) return

        val oldMode = _callMode.value
        Log.d(TAG, "Switching call mode from $oldMode to $mode")

        if (broadcast) {
            scope.launch {
                try {
                    val message = "MODE:${mode.name}".toByteArray()
                    _room.value?.localParticipant?.publishData(
                        message,
                        DataPublishReliability.RELIABLE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to broadcast mode switch", e)
                }
            }
        }

        scope.launch {
            val r = _room.value
            if (mode == com.example.holoverse.webrtc.domain.model.CallMode.VIDEO) {
                updateLegacyFlags(mode)
                r?.let {
                    Log.d(TAG, "Restoring CAMERA mode")
                    // Unpublish synthetic track first
                    syntheticTrack?.let {
                        Log.d(TAG, "Unpublishing synthetic track")
                        it.stop() // Stop the track explicitly
                        r.localParticipant.unpublishTrack(it)
                    }
                    syntheticTrack = null
                    syntheticCapturer = null

                    stopSyntheticMode()
                    delay(500.milliseconds)
                    r.localParticipant.setCameraEnabled(true)
                }
                _callMode.value = mode
            } else {
                // Set the mode BEFORE starting synthetic mode to avoid race conditions in the frame loop
                _callMode.value = mode
                updateLegacyFlags(mode)
                
                r?.let {
                    Log.d(TAG, "Switching to SYNTHETIC mode: $mode")
                    // Stop camera first and wait for hardware release
                    r.localParticipant.setCameraEnabled(false)
                    delay(1200.milliseconds) 

                    startSyntheticMode()
                    ensureSyntheticTrack(r)
                    syntheticTrack?.let {
                        Log.d(TAG, "Publishing synthetic track")
                        r.localParticipant.publishVideoTrack(it)
                    }
                }
            }

            _arMirrorSurface.value =
                if (mode == com.example.holoverse.webrtc.domain.model.CallMode.AR) syntheticSurface else null
        }
    }

    private fun ensureSyntheticTrack(room: Room) {
        if (syntheticTrack == null) {
            Log.d(TAG, "Creating new synthetic video track")
            val capturer = SyntheticCapturer()
            syntheticCapturer = capturer

            val resolution = _syntheticResolution.value ?: Pair(720, 1280)
            val options = LocalVideoTrackOptions(
                isScreencast = true,
                captureParams = VideoCaptureParameter(
                    width = resolution.first,
                    height = resolution.second,
                    maxFps = 30,
                    adaptOutputToDimensions = true
                )
            )

            val track = room.localParticipant.createVideoTrack(
                name = "synthetic",
                capturer = capturer,
                options = options
            )
            syntheticTrack = track

            syntheticSurfaceTextureHelper?.let { helper ->
                Log.d(TAG, "Connecting SurfaceTextureHelper to capturer")
                helper.startListening { frame ->
                    capturer.observer?.onFrameCaptured(frame)
                }
            }
        }
    }

    private class SyntheticCapturer : VideoCapturer {
        var observer: CapturerObserver? = null
        override fun initialize(p0: SurfaceTextureHelper?, p1: Context?, p2: CapturerObserver?) {
            this.observer = p2
        }

        override fun startCapture(p0: Int, p1: Int, p2: Int) {}
        override fun stopCapture() {}
        override fun changeCaptureFormat(p0: Int, p1: Int, p2: Int) {}
        override fun dispose() {
            observer = null
        }

        override fun isScreencast(): Boolean = true
    }

    private fun updateLegacyFlags(mode: com.example.holoverse.webrtc.domain.model.CallMode) {
        _isArEnabled.value = mode == com.example.holoverse.webrtc.domain.model.CallMode.AR
        _isWhiteboardEnabled.value =
            mode == com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD
        _isPdfEnabled.value = mode == com.example.holoverse.webrtc.domain.model.CallMode.PDF
    }

    private fun startSyntheticMode() {
        if (syntheticSurfaceTextureHelper != null) return

        Log.d(TAG, "Starting synthetic mode capture (Surface + Loop)")

        syntheticSurfaceTextureHelper =
            SurfaceTextureHelper.create("SyntheticThread", eglBaseContext)

        // Calculate target resolution based on device aspect ratio
        val metrics = context.resources.displayMetrics
        val maxWidth = 1920
        val maxHeight = 1920
        val scale = min(
            maxWidth.toFloat() / metrics.widthPixels,
            maxHeight.toFloat() / metrics.heightPixels
        ).coerceAtMost(1.0f)
        
        // Ensure multiples of 16 for maximum hardware encoder compatibility
        val targetWidth = ((metrics.widthPixels * scale).toInt() / 16) * 16
        val targetHeight = ((metrics.heightPixels * scale).toInt() / 16) * 16
        
        Log.d(TAG, "Synthetic Resolution (Optimized for HW): ${targetWidth}x${targetHeight}")
        _syntheticResolution.value = Pair(targetWidth, targetHeight)
        
        syntheticSurfaceTextureHelper?.setTextureSize(targetWidth, targetHeight)
        syntheticSurface = Surface(syntheticSurfaceTextureHelper?.surfaceTexture)

        syntheticLoopJob = scope.launch(Dispatchers.Default) {
            val paint =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
            val clearPaint = android.graphics.Paint().apply { color = Color.WHITE }

            while (_callMode.value != com.example.holoverse.webrtc.domain.model.CallMode.VIDEO) {
                // Determine target FPS based on mode
                val frameDelay =
                    if (_callMode.value == com.example.holoverse.webrtc.domain.model.CallMode.AR) {
                        33L // ~30 FPS for AR
                    } else {
                        200L // 5 FPS for PDF/Whiteboard to prioritize clarity
                    }

                // IMPORTANT: In AR mode, the AR engine (Filament) draws directly to the surface.
                if (_callMode.value != com.example.holoverse.webrtc.domain.model.CallMode.AR) {
                    val canvas = syntheticSurface?.lockCanvas(null)
                    if (canvas != null) {
                        try {
                            when (_callMode.value) {
                                com.example.holoverse.webrtc.domain.model.CallMode.WHITEBOARD -> {
                                    whiteboardManager?.drawToNativeCanvas(canvas)
                                        ?: canvas.drawRect(
                                            0f,
                                            0f,
                                            canvas.width.toFloat(),
                                            canvas.height.toFloat(),
                                            clearPaint
                                        )
                                }

                                com.example.holoverse.webrtc.domain.model.CallMode.PDF -> {
                                    pdfManager.currentBitmap.value?.let { bitmap ->
                                        val scaleFit = min(
                                            canvas.width.toFloat() / bitmap.width,
                                            canvas.height.toFloat() / bitmap.height
                                        )
                                        val dstWidth = (bitmap.width * scaleFit).toInt()
                                        val dstHeight = (bitmap.height * scaleFit).toInt()
                                        val left = (canvas.width - dstWidth) / 2
                                        val top = (canvas.height - dstHeight) / 2
                                        canvas.drawRect(
                                            0f,
                                            0f,
                                            canvas.width.toFloat(),
                                            canvas.height.toFloat(),
                                            clearPaint
                                        )
                                        canvas.drawBitmap(
                                            bitmap,
                                            null,
                                            Rect(left, top, left + dstWidth, top + dstHeight),
                                            paint
                                        )
                                    } ?: canvas.drawRect(
                                        0f,
                                        0f,
                                        canvas.width.toFloat(),
                                        canvas.height.toFloat(),
                                        clearPaint
                                    )
                                    whiteboardManager?.drawToNativeCanvas(
                                        canvas,
                                        clearBackground = false
                                    )
                                }

                                else -> {}
                            }
                        } finally {
                            syntheticSurface?.unlockCanvasAndPost(canvas)
                        }
                    }
                }

                delay(frameDelay.milliseconds)
            }
        }

        // Re-bind if track already exists (unlikely given null checks, but safe)
        syntheticTrack?.let {
            syntheticSurfaceTextureHelper?.startListening { frame ->
                syntheticCapturer?.observer?.onFrameCaptured(frame)
            }
        }
    }

    private fun stopSyntheticMode() {
        syntheticLoopJob?.cancel()
        syntheticLoopJob = null
        syntheticSurfaceTextureHelper?.dispose()
        syntheticSurfaceTextureHelper = null
        syntheticSurface?.release()
        syntheticSurface = null
        syntheticCapturer = null
        syntheticTrack = null
        _arMirrorSurface.value = null
        _syntheticResolution.value = null
    }

    fun getEglContext() = eglBaseContext
    fun loadPdf(uri: android.net.Uri) = pdfManager.loadPdf(uri)
    fun pdfNextPage() = pdfManager.nextPage()
    fun pdfPreviousPage() = pdfManager.previousPage()
}
