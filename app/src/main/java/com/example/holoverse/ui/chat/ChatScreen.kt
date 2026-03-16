package com.example.holoverse.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.currentChatId == null) {
        ChatListScreen(uiState, viewModel)
    } else {
        ConversationScreen(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}
