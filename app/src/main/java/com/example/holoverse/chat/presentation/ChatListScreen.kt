package com.example.holoverse.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.holoverse.R
import com.example.holoverse.chat.presentation.components.ChatListItem
import com.example.holoverse.chat.presentation.components.ContactListItem
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    darkTheme: Boolean,
    onContactSelected: (String) -> Unit
) {
    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(headerBrush)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.messages),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbarraNovaFont
                        ),

                        )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.chat_connect_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val searchQuery = uiState.searchQuery
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = {
                            Text(
                                stringResource(R.string.search_mentors),
                                //   color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear)
                                    )
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.chats.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.recent_chats),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(
                            items = uiState.chats,
                            key = { it.id },
                            contentType = { "chat_item" }
                        ) { chat ->
                            val currentUserId = uiState.currentUser?.userId ?: ""
                            val isGroup = chat.id.startsWith("group_")

                            val partnerId =
                                if (isGroup) null else chat.participants.find { it != currentUserId }
                                    ?: chat.participants.firstOrNull { it != "user1" }

                            val chatName = if (isGroup) {
                                chat.participantNames[chat.id] ?: chat.id.removePrefix("group_")
                            } else {
                                chat.participantNames[partnerId]
                                    ?: stringResource(R.string.chat_fallback)
                            }

                            val chatImageUrl =
                                if (isGroup) null else chat.participantProfileImages[partnerId]

                            ChatListItem(
                                name = chatName,
                                lastMessage = chat.lastMessage,
                                imageUrl = chatImageUrl,
                                onClick = {
                                    onContactSelected(chat.id)
                                }
                            )
                        }
                    }

                    if (uiState.filteredContacts.isNotEmpty()) {
                        item {
                            Text(
                                if (uiState.searchQuery.isEmpty()) stringResource(R.string.suggested_contacts) else stringResource(
                                    R.string.search_results_title
                                ),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(
                            items = uiState.filteredContacts,
                            key = { it.userId ?: it.hashCode() },
                            contentType = { "contact_item" }
                        ) { mentor ->
                            ContactListItem(
                                mentor = mentor,
                                onClick = {
                                    mentor.userId?.let { onContactSelected(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


