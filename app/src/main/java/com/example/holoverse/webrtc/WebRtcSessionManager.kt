package com.example.holoverse.webrtc

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import org.webrtc.PeerConnectionFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.DefaultVideoDecoderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signalingClient: SignalingClient
) {
    private val TAG = "WebRtcSessionManager"
    private val eglBaseContext: EglBase.Context = EglBase.create().eglBaseContext
    private val scope = CoroutineScope(Dispatchers.Main + Job())

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

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack

    fun init(callId: String, isOffer: Boolean) {
        setupPeerConnection(callId, isOffer)
        setupLocalVideo()
        observeSignaling(callId, isOffer)
    }

    private fun setupPeerConnection(callId: String, isOffer: Boolean) {
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            )
        ).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                
                override fun onIceCandidate(candidate: IceCandidate) {
                    signalingClient.sendIceCandidate(callId, candidate, isOffer)
                }

                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                override fun onAddStream(p0: MediaStream?) {}
                override fun onRemoveStream(p0: MediaStream?) {}
                
                override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                
                override fun onAddTrack(receiver: org.webrtc.RtpReceiver?, streams: Array<out MediaStream>?) {
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
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)

        localVideoTrackInternal = peerConnectionFactory.createVideoTrack(
            "VideoTrack",
            videoSource
        )
        _localVideoTrack.value = localVideoTrackInternal
        
        peerConnection?.addTrack(localVideoTrackInternal)
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
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
                    pendingIceCandidates.add(candidate)
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
        if (peerConnection?.signalingState() != PeerConnection.SignalingState.STABLE) {
            Log.w(TAG, "Ignoring offer because signaling state is ${peerConnection?.signalingState()}")
            return
        }
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
            Log.w(TAG, "Ignoring answer because signaling state is ${peerConnection?.signalingState()}")
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
        Log.d(TAG, "Draining ${pendingIceCandidates.size} pending ICE candidates")
        pendingIceCandidates.forEach {
            peerConnection?.addIceCandidate(it)
        }
        pendingIceCandidates.clear()
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting")
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        peerConnection?.close()
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null
        isRemoteDescriptionSet = false
        pendingIceCandidates.clear()
    }

    fun getEglContext() = eglBaseContext
}
