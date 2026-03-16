package com.example.holoverse.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.repository.ChatRepository
import com.example.holoverse.fetch.domain.FetchDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import java.io.File
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val fetchDataRepository: FetchDataRepository,
    private val cloudinaryRepository: CloudinaryRepository,
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
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.getCurrentUser()
            val mentors = fetchDataRepository.fetchMentors()
            
            _uiState.update { it.copy(
                currentUser = user,
                contacts = mentors,
                isLoading = false
            ) }

            user?.userId?.let { userId ->
                // This triggers the repository to start syncing and exposing the flow
                chatRepository.getChats(userId).collectLatest { /* Handled by observeRepositoryState */ }
            }
        }
    }

    private fun observeRepositoryState() {
        viewModelScope.launch {
            combine(
                chatRepository.chatsState,
                chatRepository.messagesState,
                _uiState
            ) { repoChats, repoMessagesMap, currentState ->
                val currentChatId = currentState.currentChatId
                val messagesForCurrentChat = if (currentChatId != null) {
                    repoMessagesMap[currentChatId] ?: emptyList()
                } else {
                    emptyList()
                }

                currentState.copy(
                    chats = repoChats,
                    messages = messagesForCurrentChat
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun onTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun onContactSelected(mentor: User.Mentor) {
        val currentUser = _uiState.value.currentUser ?: return
        val otherUserId = mentor.userId ?: return
        val currentUserId = currentUser.userId ?: return
        
        _uiState.update { it.copy(
            selectedChatPartnerName = mentor.fullName ?: "Chat",
            isLoading = true 
        ) }

        viewModelScope.launch {
            val chatId = chatRepository.createOrGetChat(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                currentUserName = currentUser.fullName ?: "User",
                otherUserName = mentor.fullName ?: "Mentor"
            )
            _uiState.update { it.copy(currentChatId = chatId, isLoading = false) }
            // Trigger message observation in repo
            chatRepository.getMessages(chatId).collectLatest { }
        }
    }
    
    fun onChatSelected(chat: Chat) {
        val currentUserId = _uiState.value.currentUser?.userId ?: return
        val partnerName = chat.participantNames.filterKeys { it != currentUserId }.values.firstOrNull() ?: "Chat"
        
        _uiState.update { it.copy(
            selectedChatPartnerName = partnerName,
            currentChatId = chat.id
        ) }
        
        viewModelScope.launch {
            chatRepository.getMessages(chat.id).collectLatest { }
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

    fun startRecording() {
        try {
            audioFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.m4a")
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
    }

    private fun sendVoiceMessage(audioUrl: String) {
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
                        text = "",
                        senderId = senderId,
                        senderName = senderName,
                        senderType = senderType,
                        audioUrl = audioUrl
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}
