package com.example.holoverse.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ChatScreen(
    darkTheme: Boolean,
    mentorId: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToConversation: ((String) -> Unit)? = null,
    onNavigateToVideoCall: ((String, String, String?) -> Unit)? = null,
    onNavigateToViewer: ((String, String) -> Unit)? = null,
    onIncomingCall: ((String, String, String?) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onGroupInfoClick: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mentorId) {
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
            onIncomingCall = onIncomingCall,
            onGlbClick = onNavigateToViewer,
            onGroupInfoClick = onGroupInfoClick
        )
    }
}

