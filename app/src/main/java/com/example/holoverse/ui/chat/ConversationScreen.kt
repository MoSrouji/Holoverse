package com.example.holoverse.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.ui.chat.components.ChatInput
import com.example.holoverse.ui.chat.components.EmojiPicker
import com.example.holoverse.ui.chat.components.MessageBubble
import com.example.holoverse.ui.chat.components.SendingVoiceBubble
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.spatialTheme.SpatialBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    onBackClick: (() -> Unit)? = null,
    darkTheme: Boolean = true
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAndSendFile(it, "image") }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAndSendFile(it, "video") }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
            viewModel.uploadAndSendFile(it, "pdf", fileName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // ... (topBar content)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .clip(
//                        RoundedCornerShape(
//                            bottomStart = 32.dp,
//                            bottomEnd = 32.dp
//                        )
//                    )
                    . background (Brush(darkTheme))

            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (uiState.selectedChatPartnerImageUrl != null) {
                                        AsyncImage(
                                            model = uiState.selectedChatPartnerImageUrl,
                                            contentDescription = uiState.selectedChatPartnerName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            uiState.selectedChatPartnerName.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                uiState.selectedChatPartnerName,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (onBackClick != null) {
                                onBackClick()
                            } else {
                                viewModel.backToChatList()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },

                )
            }
        },
        bottomBar = {
            ChatInput(
                text = uiState.inputText,
                isRecording = uiState.isRecording,
                onTextChange = viewModel::onTextChanged,
                onSend = viewModel::sendMessage,
                onMediaClick = { type ->
                    when (type) {
                        "image" -> imagePickerLauncher.launch("image/*")
                        "video" -> videoPickerLauncher.launch("video/*")
                        "pdf" -> filePickerLauncher.launch("application/pdf")
                    }
                },
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
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (uiState.isSendingAudio || uiState.isUploadingFile) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                if (uiState.isSendingAudio) {
                    item {
                        SendingVoiceBubble()
                    }
                }

                items(uiState.messages.asReversed()) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = (uiState.currentUser?.userId ?: "") == message.senderId,
                        isPlaying = uiState.playingAudioUrl == message.audioUrl && message.audioUrl != null,
                        onPlayClick = { message.audioUrl?.let { viewModel.playAudio(it) } }
                    )
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
