package com.example.holoverse.ui.chat

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.chat_system.domain.model.Message

data class ChatUiState(
    val chats: List<Chat> = emptyList(),
    val messages: List<Message> = emptyList(),
    val contacts: List<User.Mentor> = emptyList(),
    val filteredContacts: List<User.Mentor> = emptyList(),
    val searchQuery: String = "",
    val currentUser: User? = null,
    val currentChatId: String? = null,
    val selectedChatPartnerName: String = "",
    val selectedChatPartnerImageUrl: String? = null,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val isSendingAudio: Boolean = false,
    val isUploadingFile: Boolean = false,
    val playingAudioUrl: String? = null
)
