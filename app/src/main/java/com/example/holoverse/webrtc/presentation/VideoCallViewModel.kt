package com.example.holoverse.webrtc.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.webrtc.domain.model.Participant
import com.example.holoverse.webrtc.domain.usecase.EndCallUseCase
import com.example.holoverse.webrtc.domain.usecase.GetEglContextUseCase
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
import com.example.holoverse.webrtc.domain.usecase.ObserveRemoteParticipantsUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveRemoteVideoTrackUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveSignalingUseCase
import com.example.holoverse.webrtc.domain.usecase.ObserveSyntheticResolutionUseCase
import com.example.holoverse.webrtc.domain.usecase.PdfNextPageUseCase
import com.example.holoverse.webrtc.domain.usecase.PdfPreviousPageUseCase
import com.example.holoverse.webrtc.domain.usecase.SetCallModeUseCase
import com.example.holoverse.webrtc.domain.usecase.SetWhiteboardManagerUseCase
import com.example.holoverse.webrtc.domain.usecase.SwitchCameraUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleArModeUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleCameraUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleMuteUseCase
import com.example.holoverse.webrtc.domain.usecase.TogglePdfModeUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleSpeakerUseCase
import com.example.holoverse.webrtc.domain.usecase.ToggleWhiteboardModeUseCase
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.domain.model.Message
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val endCallUseCase: EndCallUseCase,
    private val observeLocalVideoTrackUseCase: ObserveLocalVideoTrackUseCase,
    private val observeRemoteVideoTrackUseCase: ObserveRemoteVideoTrackUseCase,
    private val observeRemoteParticipantsUseCase: ObserveRemoteParticipantsUseCase,
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
    private val observeSyntheticResolutionUseCase: ObserveSyntheticResolutionUseCase,
    private val observeSignalingUseCase: ObserveSignalingUseCase,
    private val getEglContextUseCase: GetEglContextUseCase,
    private val sendInviteUseCase: com.example.holoverse.webrtc.domain.usecase.SendInviteUseCase,
    private val repository: WebRtcRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val localVideoTrack: StateFlow<io.livekit.android.room.track.VideoTrack?> = observeLocalVideoTrackUseCase()
    val remoteVideoTrack: StateFlow<livekit.org.webrtc.VideoTrack?> = observeRemoteVideoTrackUseCase()
    val remoteParticipants: StateFlow<List<Participant>> = observeRemoteParticipantsUseCase()
    val room = repository.room // Expose the room object
    val connectionState = observeConnectionStateUseCase()
    val isCallEnded = observeIsCallEndedUseCase()
    val isArEnabled = observeIsArEnabledUseCase()
    val isWhiteboardEnabled = observeIsWhiteboardEnabledUseCase()
    val isPdfEnabled = observeIsPdfEnabledUseCase()
    val callMode = observeCallModeUseCase()
    val pdfBitmap = observePdfBitmapUseCase()
    val arMirrorSurface = observeArMirrorSurfaceUseCase()
    val syntheticResolution = observeSyntheticResolutionUseCase()
    val isMessagingRestricted = repository.isMessagingRestricted

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isChatVisible = MutableStateFlow(true)
    val isChatVisible: StateFlow<Boolean> = _isChatVisible.asStateFlow()

    private val _currentUserIsMentor = MutableStateFlow(false)
    val currentUserIsMentor: StateFlow<Boolean> = _currentUserIsMentor.asStateFlow()

    private val _mentorId = MutableStateFlow<String?>(null)
    val mentorId: StateFlow<String?> = _mentorId.asStateFlow()

    private val _isGridViewEnabled = MutableStateFlow(false)
    val isGridViewEnabled: StateFlow<Boolean> = _isGridViewEnabled.asStateFlow()

    private var currentRoomId: String? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    private var initJob: kotlinx.coroutines.Job? = null

    fun startSignaling(callId: String, isOffer: Boolean) {
        android.util.Log.d("VideoCallViewModel", "startSignaling: signalingId=$callId, roomId=$currentRoomId")
        observeSignalingUseCase(callId)
        
        // Messages and Mentor status should always use the Room ID (Chat ID)
        val roomId = currentRoomId ?: callId
        observeMessages(roomId)
        checkMentorStatus(roomId, isOffer)
    }

    private fun observeMessages(chatId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getCallMessages(chatId).collectLatest { allMessages ->
                // Take only the last 5 messages
                _messages.value = allMessages.takeLast(5)
            }
        }
    }

    private fun checkMentorStatus(chatId: String, isOffer: Boolean) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            if (chatId.startsWith("group_")) {
                chatRepository.getChats(user.userId ?: "").collectLatest { chats ->
                    val chat = chats.find { it.id == chatId }
                    // Only the group creator is the owner/mentor
                    val isMentor = chat?.creatorId == user.userId
                    _currentUserIsMentor.value = isMentor
                    _mentorId.value = chat?.creatorId
                }
            } else {
                // In private chat, the owner is the one who initiated the call
                _currentUserIsMentor.value = isOffer
                if (isOffer) {
                    _mentorId.value = user.userId
                } else {
                    // Mentor is the other person
                    val participants = chatId.split("_")
                    _mentorId.value = participants.find { it != user.userId }
                }
            }
        }
    }

    fun initCall(roomId: String, inviteId: String?, isOffer: Boolean) {
        this.currentRoomId = roomId
        android.util.Log.d("VideoCallViewModel", "initCall: roomId=$roomId, inviteId=$inviteId, isOffer=$isOffer")
        
        // Start signaling first
        startSignaling(inviteId ?: roomId, isOffer)

        initJob?.cancel()
        initJob = viewModelScope.launch {
            val url = com.example.holoverse.BuildConfig.LIVEKIT_URL
            
            try {
                // Fetch dynamic token from our Firebase Function
                val token = repository.getJoinToken(roomId)
                
                if (token.isNotEmpty()) {
                    val finalInviteId = if (isOffer) {
                        inviteId ?: run {
                            val participants = roomId.split("_")
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            val recipientId = participants.find { it != currentUserId }
                            if (recipientId != null) "invite_${roomId}_$recipientId" else null
                        }
                    } else {
                        inviteId // On recipient side, inviteId is passed from MainActivity
                    }

                    if (isOffer) {
                        val participants = roomId.split("_")
                        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        val recipientId = participants.find { it != currentUserId }
                        if (recipientId != null) {
                            android.util.Log.d("VideoCallViewModel", "Sending invite before joining room: $roomId for $recipientId")
                            sendInviteUseCase(roomId, recipientId)
                        }
                    }

                    repository.joinRoom(
                        url = url,
                        token = token,
                        roomId = roomId,
                        inviteId = finalInviteId
                    )
                } else {
                    android.util.Log.e("VideoCallViewModel", "Token is empty")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    android.util.Log.d("VideoCallViewModel", "initCall cancelled (normal re-composition or user navigation)")
                } else {
                    android.util.Log.e("VideoCallViewModel", "Failed to initialize call", e)
                }
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            if (_currentUserIsMentor.value) {
                currentRoomId?.let { chatRepository.cleanupCallMessages(it) }
            }
            endCallUseCase()
        }
    }

    fun sendMessage(text: String) {
        val roomId = currentRoomId ?: return
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            try {
                chatRepository.sendMessage(
                    chatId = roomId,
                    text = text,
                    senderId = user.userId ?: "",
                    senderName = user.fullName ?: "Unknown",
                    senderType = user.accountType.name,
                    isCallMessage = true
                )
            } catch (e: Exception) {
                android.util.Log.e("VideoCallViewModel", "Failed to send in-call message", e)
            }
        }
    }

    fun toggleChatVisibility() {
        _isChatVisible.update { !it }
    }

    fun toggleGridView() {
        _isGridViewEnabled.update { !it }
    }

    fun toggleMessagingRestriction() {
        val current = repository.isMessagingRestricted.value
        repository.setMessagingRestricted(!current)
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
