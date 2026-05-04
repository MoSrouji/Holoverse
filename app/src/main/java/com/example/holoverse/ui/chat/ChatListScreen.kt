package com.example.holoverse.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.chat_system.domain.model.Chat
import com.example.holoverse.ui.chat.components.ChatListItem
import com.example.holoverse.ui.chat.components.ContactListItem
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    darkTheme: Boolean,
    onContactSelected: (String) -> Unit
) {
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
                    .background(Brush(darkTheme))

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbarraNovaFont
                        ),

                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connect with your mentors and peers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { 
                            Text(
                                "Search mentors...",
                             //   color = Color.White.copy(alpha = 0.5f)
                            ) 
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
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
                            "Recent Chats",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(uiState.chats) { chat ->
                        val currentUserId = uiState.currentUser?.userId ?: ""
                        val partnerId = chat.participants.find { it != currentUserId }
                        val partnerName = chat.participantNames[partnerId] ?: "Chat"
                        val partnerImageUrl = chat.participantProfileImages[partnerId]
                        
                        ChatListItem(
                            name = partnerName,
                            lastMessage = chat.lastMessage,
                            imageUrl = partnerImageUrl,
                            onClick = { 
                                if (partnerId != null) {
                                    onContactSelected(partnerId)
                                }
                            }
                        )
                    }
                }

                item {
                    Text(
                        if (uiState.searchQuery.isEmpty()) "Suggested Contacts" else "Search Results",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(uiState.filteredContacts) { mentor ->
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
