package com.example.holoverse.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.chat_system.domain.model.Message
import com.example.holoverse.chat_system.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)

    // This should ideally be passed via SavedStateHandle from navigation
    private var currentChatId: String = "default_chat"

    init {
        loadCurrentUser()
        observeMessages()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(currentChatId).collect {
                _messages.value = it
            }
        }
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage() {
        val text = _inputText.value
        val user = _currentUser.value
        
        if (text.isNotBlank() && user != null) {
            viewModelScope.launch {
                val senderId = user.userId ?: ""
                val senderName = user.fullName ?: "Unknown"
                val senderType = user.accountType.name
                
                _inputText.value = ""
                try {
                    chatRepository.sendMessage(
                        chatId = currentChatId,
                        text = text,
                        senderId = senderId,
                        senderName = senderName,
                        senderType = senderType
                    )
                } catch (e: Exception) {
                    // Handle error (e.g., show a toast or error state)
                }
            }
        }
    }
    
    // Function to set chatId if it's dynamic
    fun setChatId(chatId: String) {
        currentChatId = chatId
        observeMessages()
    }
}
