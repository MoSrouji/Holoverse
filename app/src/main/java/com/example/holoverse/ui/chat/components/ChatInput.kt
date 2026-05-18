package com.example.holoverse.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatInput(
    text: String,
    isRecording: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMediaClick: (String) -> Unit,
    onEmojiClick: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit
) {
    var showAttachmentMenu by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                // ... (existing recording UI)
                Text(
                    text = "Recording...",
                    color = Color.Red,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = onCancelRecording) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel recording",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = onStopRecording) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Stop and send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(onClick = onEmojiClick) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Emoji picker",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 4
                )
                Box {
                    IconButton(onClick = { showAttachmentMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Send multimedia",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showAttachmentMenu,
                        onDismissRequest = { showAttachmentMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Photo") },
                            onClick = {
                                showAttachmentMenu = false
                                onMediaClick("image")
                            },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Video") },
                            onClick = {
                                showAttachmentMenu = false
                                onMediaClick("video")
                            },
                            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("PDF") },
                            onClick = {
                                showAttachmentMenu = false
                                onMediaClick("pdf")
                            },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Voice") },
                            onClick = {
                                showAttachmentMenu = false
                                onStartRecording()
                            },
                            leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
                        )
                    }
                }
                IconButton(
                    onClick = { 
                        if (text.isNotBlank()) onSend() else onStartRecording()
                    },
                    enabled = true
                ) {
                    Icon(
                        imageVector = if (text.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (text.isNotBlank()) "Send" else "Voice recorder",
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
