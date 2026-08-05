package com.example.holoverse.chat.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.chat.presentation.components.AttachmentSheet
import com.example.holoverse.chat.presentation.components.ChatInput
import com.example.holoverse.chat.presentation.components.CreateBookingDialog
import com.example.holoverse.chat.presentation.components.CreatePollDialog
import com.example.holoverse.chat.presentation.components.EmojiPicker
import com.example.holoverse.chat.presentation.components.MessageBubble
import com.example.holoverse.chat.presentation.components.SendingVoiceBubble
import com.example.holoverse.core.ui.spatial.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    onBackClick: (() -> Unit)? = null,
    onVideoCallClick: ((String, String, String?) -> Unit)? = null,
    onIncomingCall: ((String, String, String?) -> Unit)? = null,
    onGlbClick: ((String, String) -> Unit)? = null,
    onGroupInfoClick: ((String) -> Unit)? = null,
    darkTheme: Boolean = true
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

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

    val glbPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName =
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            viewModel.uploadAndSendFile(it, "glb", fileName)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName =
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            viewModel.uploadAndSendFile(it, "pdf", fileName)
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName =
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            viewModel.uploadAndSendFile(it, "audio", fileName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    LaunchedEffect(uiState.incomingCallId) {
        uiState.incomingCallId?.let { callId ->
            onIncomingCall?.invoke(
                callId,
                uiState.selectedChatPartnerName,
                uiState.selectedChatPartnerImageUrl
            )
            viewModel.onIncomingCallHandled()
        }
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
                    .background(headerBrush)
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (uiState.currentChatId != null && uiState.currentChatId.startsWith("group_")) {
                                    onGroupInfoClick?.invoke(uiState.currentChatId)
                                }
                            }
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
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
                                            if (uiState.selectedChatPartnerName.isEmpty()) "L" else uiState.selectedChatPartnerName.take(
                                                1
                                            ).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.selectedChatPartnerName.ifEmpty { stringResource(R.string.chat_fallback) },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (darkTheme) Color.White else Color.Black
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
                                contentDescription = stringResource(R.string.back),
                                tint = if (darkTheme) Color.White else Color.Black
                            )
                        }
                    },
                    actions = {
                        if (uiState.currentChatId != null) {
                            IconButton(onClick = {
                                onVideoCallClick?.invoke(
                                    uiState.currentChatId,
                                    uiState.selectedChatPartnerName,
                                    uiState.selectedChatPartnerImageUrl
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = stringResource(R.string.video_call),
                                    tint = if (darkTheme) Color.White else Color.Black
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            ChatInput(
                text = uiState.inputText,
                isRecording = uiState.isRecording,
                onTextChange = viewModel::onTextChanged,
                onSend = viewModel::sendMessage,
                onAttachmentClick = { showAttachmentSheet = true },
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
                onCancelRecording = viewModel::cancelRecording,
                isRestricted = uiState.isRestricted,
                restrictionMessage = if (uiState.currentChat?.isOnlyMentorMessaging == true && !uiState.currentUserIsMentor) 
                    "Only the mentor can send messages" 
                else "You are restricted from sending messages"
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
                    item(
                        key = "uploading_indicator",
                        contentType = "system_status"
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                if (uiState.isSendingAudio) {
                    item(
                        key = "sending_voice_indicator",
                        contentType = "system_status"
                    ) {
                        SendingVoiceBubble()
                    }
                }

                items(
                    items = uiState.messages.asReversed(),
                    key = { it.id },
                    contentType = { "chat_message" }
                ) { message ->
                    val isCurrentUser = (uiState.currentUser?.userId ?: "") == message.senderId
                    val isGroup = uiState.currentChat?.isGroup == true
                    val senderImageUrl = if (isGroup) uiState.currentChat?.participantProfileImages?.get(message.senderId) else null

                    MessageBubble(
                        message = message,
                        isCurrentUser = isCurrentUser,
                        isPlaying = uiState.playingAudioUrl == message.audioUrl && message.audioUrl != null,
                        onPlayClick = { message.audioUrl?.let { viewModel.playAudio(it) } },
                        onGlbClick = onGlbClick,
                        onVoteClick = { optionIdx -> 
                            if (message.poll != null) viewModel.voteOnPoll(message.id, optionIdx) 
                            // Add logic for booking vote if needed
                        },
                        onFinalizeBooking = { selectedTime -> 
                            message.bookingRequest?.let { request ->
                                viewModel.respondToBookingRequest(
                                    messageId = message.id, 
                                    status = "CONFIRMED",
                                    sessionId = request.sessionId,
                                    selectedTime = selectedTime
                                )
                            }
                        },
                        senderImageUrl = senderImageUrl,
                        showSenderInfo = isGroup,
                        currentUserId = uiState.currentUser?.userId ?: ""
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

            if (showAttachmentSheet) {
                AttachmentSheet(
                    onDismiss = { showAttachmentSheet = false },
                    onMediaClick = { type ->
                        when (type) {
                            "image" -> imagePickerLauncher.launch("image/*")
                            "video" -> videoPickerLauncher.launch("video/*")
                            "glb" -> glbPickerLauncher.launch("*/*")
                            "pdf" -> filePickerLauncher.launch("application/pdf")
                            "audio" -> audioPickerLauncher.launch("audio/*")
                        }
                    },
                    onPollClick = { showPollDialog = true },
                    onBookingClick = if (uiState.currentUserIsMentor) { { showBookingDialog = true } } else null,
                    darkTheme = darkTheme
                )
            }

            if (showPollDialog) {
                CreatePollDialog(
                    onDismiss = { showPollDialog = false },
                    onCreate = { question, options ->
                        viewModel.sendPoll(question, options)
                        showPollDialog = false
                    },
                    darkTheme = darkTheme
                )
            }
            if (showBookingDialog) {
                CreateBookingDialog(
                    onDismiss = { showBookingDialog = false },
                    onCreate = { times ->
                        uiState.currentChatId?.let { chatId ->
                            val batchId = chatId.removePrefix("group_")
                            viewModel.sendBookingRequest(
                                batchId = batchId,
                                sessionId = "session_${System.currentTimeMillis()}", // Generate a real ID in repo
                                proposedTimes = times
                            )
                        }
                        showBookingDialog = false
                    },
                    darkTheme = darkTheme
                )
            }
        }
    }
}


