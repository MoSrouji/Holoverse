package com.example.holoverse.chat.presentation

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.data.repository.ChatRepositoryImpl
import com.example.holoverse.chat.domain.model.Chat
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.webrtc.data.datasource.SignalingClient
import com.example.holoverse.webrtc.data.datasource.SignalingEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val fetchDataRepository: FetchDataRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    private val signalingClient: SignalingClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeRepositoryState()
        observeIncomingCalls()

    }

    private fun observeIncomingCalls() {
        viewModelScope.launch {
            _uiState.map { it.currentChatId }.distinctUntilChanged().collectLatest { chatId ->
                if (chatId == null) return@collectLatest
                signalingClient.observeCall(chatId).collectLatest { event ->
                    if (event is SignalingEvent.OfferReceived) {
                        _uiState.update { it.copy(incomingCallId = chatId) }
                    }
                }
            }
        }
    }

    fun onIncomingCallHandled() {
        _uiState.update { it.copy(incomingCallId = null) }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. Get cached user first for immediate offline availability
            val cachedUser = authRepository.getCachedUser()
            if (cachedUser != null) {
                _uiState.update { it.copy(currentUser = cachedUser) }
                cachedUser.userId?.let { userId ->
                    // Start observing local chats immediately
                    launch {
                        chatRepository.getChats(userId).collectLatest { }
                    }
                }
            } else {
                // Only show loading if we don't even have a cached user
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                // 2. Refresh current user from remote (updates cache)
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _uiState.update { it.copy(currentUser = user) }
                    // If the user changed or we didn't have a cached user, trigger chat sync
                    if (cachedUser?.userId != user.userId) {
                        user.userId?.let { userId ->
                            launch {
                                chatRepository.getChats(userId).collectLatest { }
                            }
                        }
                    }
                }

                // 3. Fetch mentors (Repository handles internal caching)
                val mentors = fetchDataRepository.fetchMentors()

                _uiState.update {
                    it.copy(
                        contacts = mentors,
                        filteredContacts = mentors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // If network fails, stop loading but keep cached data
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeRepositoryState() {
        viewModelScope.launch {
            combine(
                chatRepository.chatsState,
                chatRepository.messagesState,
                _uiState.map { it.currentChatId }.distinctUntilChanged()
            ) { repoChats, repoMessagesMap, currentChatId ->
                Triple(repoChats, repoMessagesMap, currentChatId)
            }.collect { (repoChats, repoMessagesMap, currentChatId) ->
                _uiState.update { currentState ->
                    val messagesForCurrentChat = if (currentChatId != null) {
                        repoMessagesMap[currentChatId] ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Filter support chats if current user is not Admin
                    val filteredChats = if (currentState.currentUser is User.Admin) {
                        repoChats
                    } else {
                        repoChats.filter { !it.isSupportChat }
                    }

                    currentState.copy(
                        chats = filteredChats,
                        messages = messagesForCurrentChat
                    )
                }
            }
        }
    }

    fun onTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.contacts
            } else {
                state.contacts.filter {
                    it.fullName?.contains(query, ignoreCase = true) == true ||
                            it.specialization.name.contains(query, ignoreCase = true) == true ||
                            it.subjects?.any { subject ->
                                subject.contains(
                                    query,
                                    ignoreCase = true
                                )
                            } == true
                }
            }
            state.copy(
                searchQuery = query,
                filteredContacts = filtered
            )
        }
    }

    fun onContactSelected(mentor: User.Mentor) {
        viewModelScope.launch {
            try {
                // Ensure we have a current user before proceeding
                val currentUser = _uiState.value.currentUser ?: authRepository.getCurrentUser()

                if (currentUser == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val otherUserId = mentor.userId ?: return@launch
                val currentUserId = currentUser.userId ?: return@launch

                _uiState.update {
                    it.copy(
                        selectedChatPartnerName = mentor.fullName ?: "Chat",
                        selectedChatPartnerImageUrl = mentor.profileImageUrl,
                        currentUser = currentUser
                    )
                }

                val chatId = chatRepository.createOrGetChat(
                    currentUserId = currentUserId,
                    otherUserId = otherUserId,
                    currentUserName = currentUser.fullName ?: "User",
                    otherUserName = mentor.fullName ?: "Mentor",
                    currentUserImageUrl = (currentUser as? User.Student)?.profileImageUrl
                        ?: (currentUser as? User.Mentor)?.profileImageUrl,
                    otherUserImageUrl = mentor.profileImageUrl
                )
                _uiState.update { it.copy(currentChatId = chatId, isLoading = false) }
                // Trigger message observation in repo
                chatRepository.getMessages(chatId).collectLatest { }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onContactSelectedById(id: String) {
        if (_uiState.value.currentChatId == id) return

        // Clear previous selection state immediately to avoid showing old conversation
        _uiState.update {
            it.copy(
                currentChatId = null,
                messages = emptyList(),
                selectedChatPartnerName = "Chat",
                selectedChatPartnerImageUrl = null,
                isLoading = true
            )
        }

        // 1. Check if it's an existing chat by ID (covers groups and private chats from list)
        val existingChatById = _uiState.value.chats.find { it.id == id }
        if (existingChatById != null) {
            onChatSelected(existingChatById)
            return
        }

        // 2. Try finding in current contacts list by userId
        val mentor = _uiState.value.contacts.find { it.userId == id }
        if (mentor != null) {
            onContactSelected(mentor)
            return
        }

        // 3. Try finding in existing chats where the ID is a participant (for offline support)
        val existingChatByParticipant = _uiState.value.chats.find { it.participants.contains(id) }
        if (existingChatByParticipant != null) {
            val partnerName = existingChatByParticipant.participantNames[id] ?: "Chat"
            val partnerImageUrl = existingChatByParticipant.participantProfileImages[id]

            // Create a temporary mentor object to trigger onContactSelected
            val tempMentor = User.Mentor(
                userId = id,
                fullName = partnerName,
                profileImageUrl = partnerImageUrl
            )
            onContactSelected(tempMentor)
            return
        }

        // 3. Last resort: Fetch from remote
        viewModelScope.launch {
            try {
                val fetchedMentor = fetchDataRepository.fetchMentorById(id)
                if (fetchedMentor != null) {
                    onContactSelected(fetchedMentor)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onChatSelected(chat: Chat) {
        val currentUserId = _uiState.value.currentUser?.userId ?: return
        val isGroup = chat.id.startsWith("group_")

        val partnerId = if (isGroup) null else chat.participants.find { it != currentUserId }
        val partnerName = if (isGroup) {
            chat.participantNames[chat.id] ?: chat.id.removePrefix("group_")
        } else {
            chat.participantNames[partnerId] ?: "Chat"
        }
        val partnerImageUrl = if (isGroup) null else chat.participantProfileImages[partnerId]

        _uiState.update {
            it.copy(
                selectedChatPartnerName = partnerName,
                selectedChatPartnerImageUrl = partnerImageUrl,
                currentChatId = chat.id
                // Removed isLoading = true to allow instant transition to cached messages
            )
        }

        viewModelScope.launch {
            chatRepository.getMessages(chat.id).collectLatest {
                // Messages are now observed via observeRepositoryState, 
                // but we can use this emission to signal sync completion if needed.
            }
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText
        val user = state.currentUser
        val chatId = state.currentChatId

        if (text.isNotBlank() && user != null && chatId != null) {
            viewModelScope.launch {
                val senderId = user.userId ?: ""
                val senderName = user.fullName ?: "Unknown"
                val senderType = user.accountType.name

                _uiState.update { it.copy(inputText = "") }
                try {
                    chatRepository.sendMessage(
                        chatId = chatId,
                        text = text,
                        senderId = senderId,
                        senderName = senderName,
                        senderType = senderType
                    )
                } catch (e: Exception) {
                    // Handle error if needed
                }
            }
        }
    }

    fun backToChatList() {
        _uiState.update { it.copy(currentChatId = null) }
    }

    fun startVideoCall(callId: String, partnerName: String, partnerImageUrl: String?) {
        Log.d("ChatViewModel", "startVideoCall: $callId, $partnerName")
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val senderName = user.fullName ?: "Unknown"
            val senderImageUrl = (user as? User.Student)?.profileImageUrl
                ?: (user as? User.Mentor)?.profileImageUrl

            chatRepository.sendCallNotification(
                chatId = callId,
                senderId = user.userId ?: "",
                senderName = senderName,
                senderImageUrl = senderImageUrl
            )
        }
    }

    fun startRecording() {
        try {
            audioFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.m4a")
            mediaRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                }
            _uiState.update { it.copy(isRecording = true) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAndSendRecording() {
        viewModelScope.launch {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                _uiState.update { it.copy(isRecording = false) }

                val file = audioFile ?: return@launch
                val uri = Uri.fromFile(file)

                _uiState.update { it.copy(isSendingAudio = true) }

                val uploadResult = cloudinaryRepository.uploadFile(uri)
                uploadResult.onSuccess { audioUrl ->
                    sendVoiceMessage(audioUrl)
                }.onFailure {
                    // Handle failure
                }
                _uiState.update { it.copy(isSendingAudio = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isRecording = false, isSendingAudio = false) }
            }
        }
    }

    fun uploadAndSendFile(uri: Uri, type: String, fileName: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUploadingFile = true) }

                val uploadResult = cloudinaryRepository.uploadFile(uri)
                uploadResult.onSuccess { url ->
                    when (type) {
                        "image" -> sendImageMessage(url)
                        "video" -> sendVideoMessage(url)
                        "glb" -> sendGlbMessage(url, fileName ?: "model.glb")
                        "pdf" -> sendFileMessage(url, fileName ?: "document.pdf")
                    }
                }.onFailure {
                    // Handle failure
                }
                _uiState.update { it.copy(isUploadingFile = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isUploadingFile = false) }
            }
        }
    }

    private fun sendImageMessage(imageUrl: String) {
        sendMessageInternal(imageUrl = imageUrl)
    }

    private fun sendVideoMessage(videoUrl: String) {
        sendMessageInternal(videoUrl = videoUrl)
    }

    private fun sendGlbMessage(glbUrl: String, fileName: String) {
        sendMessageInternal(glbUrl = glbUrl, fileName = fileName)
    }

    private fun sendFileMessage(fileUrl: String, fileName: String) {
        sendMessageInternal(fileUrl = fileUrl, fileName = fileName)
    }

    private fun sendVoiceMessage(audioUrl: String) {
        sendMessageInternal(audioUrl = audioUrl)
    }

    private fun sendMessageInternal(
        text: String = "",
        audioUrl: String? = null,
        imageUrl: String? = null,
        videoUrl: String? = null,
        glbUrl: String? = null,
        fileUrl: String? = null,
        fileName: String? = null
    ) {
        val state = _uiState.value
        val user = state.currentUser
        val chatId = state.currentChatId

        if (user != null && chatId != null) {
            viewModelScope.launch {
                val senderId = user.userId ?: ""
                val senderName = user.fullName ?: "Unknown"
                val senderType = user.accountType.name

                try {
                    chatRepository.sendMessage(
                        chatId = chatId,
                        text = text,
                        senderId = senderId,
                        senderName = senderName,
                        senderType = senderType,
                        audioUrl = audioUrl,
                        imageUrl = imageUrl,
                        videoUrl = videoUrl,
                        glbUrl = glbUrl,
                        fileUrl = fileUrl,
                        fileName = fileName
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            audioFile?.delete()
            _uiState.update { it.copy(isRecording = false) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAudio(url: String) {
        viewModelScope.launch {
            try {
                if (_uiState.value.playingAudioUrl == url) {
                    stopAudio()
                    return@launch
                }

                stopAudio()

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        _uiState.update { it.copy(playingAudioUrl = url) }
                    }
                    setOnCompletionListener {
                        _uiState.update { it.copy(playingAudioUrl = null) }
                        release()
                        mediaPlayer = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _uiState.update { it.copy(playingAudioUrl = null) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaRecorder?.release()
        (chatRepository as? ChatRepositoryImpl)?.cleanup()
    }
}

