package com.example.holoverse.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.holoverse.R
import com.example.holoverse.chat_system.domain.model.Message
import com.example.holoverse.ui.chat.components.ChatInput
import com.example.holoverse.ui.chat.components.EmojiPicker
import com.example.holoverse.ui.chat.components.MessageBubble
import com.example.holoverse.ui.chat.components.SendingVoiceBubble
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    uiState: ChatUiState,
    viewModel: ChatViewModel
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedChatPartnerName) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.backToChatList() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            ChatInput(
                text = uiState.inputText,
                isRecording = uiState.isRecording,
                onTextChange = viewModel::onTextChanged,
                onSend = viewModel::sendMessage,
                onMediaClick = { /* TODO: Implement multimedia sending */ },
                onEmojiClick = { showEmojiPicker = true },
                onStartRecording = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) -> {
                            viewModel.startRecording()
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onStopRecording = viewModel::stopAndSendRecording,
                onCancelRecording = viewModel::cancelRecording
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background Holoverse icon
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .alpha(0.1f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                reverseLayout = false,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = (uiState.currentUser?.userId ?: "") == message.senderId,
                        isPlaying = uiState.playingAudioUrl == message.audioUrl && message.audioUrl != null,
                        onPlayClick = { message.audioUrl?.let { viewModel.playAudio(it) } }
                    )
                }

                if (uiState.isSendingAudio) {
                    item {
                        SendingVoiceBubble()
                    }
                }
            }

            if (showEmojiPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showEmojiPicker = false },
                    sheetState = sheetState
                ) {
                    EmojiPicker(
                        onEmojiSelected = { emoji ->
                            viewModel.onTextChanged(uiState.inputText + emoji)
                            showEmojiPicker = false
                        }
                    )
                }
            }
        }
    }
}
