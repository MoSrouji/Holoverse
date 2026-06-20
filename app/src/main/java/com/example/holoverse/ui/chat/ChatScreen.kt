package com.example.holoverse.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ChatScreen(
    darkTheme: Boolean,
    mentorId: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToConversation: ((String) -> Unit)? = null,
    onNavigateToVideoCall: ((String) -> Unit)? = null,
    onIncomingCall: ((String) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(mentorId) {
        if (mentorId != null) {
            viewModel.onContactSelectedById(mentorId)
        } else {
            viewModel.backToChatList()
        }
    }

    if (uiState.currentChatId == null && mentorId == null) {
        ChatListScreen(
            uiState = uiState,
            viewModel = viewModel,
            darkTheme = darkTheme,
            onContactSelected = { id ->
                onNavigateToConversation?.invoke(id)
            }
        )
    } else {
        ConversationScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBackClick = onBackClick,
            onVideoCallClick = onNavigateToVideoCall,
            darkTheme = darkTheme,
            onIncomingCall = onIncomingCall
        )
    }
}
