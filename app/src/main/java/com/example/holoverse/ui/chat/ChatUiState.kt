package com.example.holoverse.ui.chat

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message

data class ChatUiState(
    val chats: List<Chat> = emptyList(),
    val messages: List<Message> = emptyList(),
    val contacts: List<User.Mentor> = emptyList(),
    val currentUser: User? = null,
    val currentChatId: String? = null,
    val selectedChatPartnerName: String = "",
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val isSendingAudio: Boolean = false,
    val playingAudioUrl: String? = null
)
