package com.example.holoverse.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.ui.chat.components.ChatListItem
import com.example.holoverse.ui.chat.components.ContactListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(uiState: ChatUiState, viewModel: ChatViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.chats.isNotEmpty()) {
                item {
                    Text(
                        "Recent Chats",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(uiState.chats) { chat ->
                    val currentUserId = uiState.currentUser?.userId ?: ""
                    val partnerName =
                        chat.participantNames.filterKeys { it != currentUserId }.values.firstOrNull()
                            ?: "Chat"
                    ChatListItem(
                        name = partnerName,
                        lastMessage = chat.lastMessage,
                        onClick = { viewModel.onChatSelected(chat) }
                    )
                }
            }

            item {
                Text(
                    "Suggested Contacts",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(uiState.contacts) { mentor ->
                ContactListItem(
                    mentor = mentor,
                    onClick = { viewModel.onContactSelected(mentor) }
                )
            }
        }
    }
}
