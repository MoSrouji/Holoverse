package com.example.holoverse.chat.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.holoverse.core.ui.spatial.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    chatId: String,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chatId) {
        viewModel.onContactSelectedById(chatId)
    }

    val chat = uiState.chats.find { it.id == chatId } ?: uiState.currentChat
    val isMentor = uiState.currentUserIsMentor || (chat?.creatorId == uiState.currentUser?.userId && chat?.isGroup == true)
    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditDescDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateGroupSettings(imageUri = it) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Group Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier.background(headerBrush)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (chat == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group Header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Surface(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable {
                                                selectedImageUrl = chat.participantProfileImages[chat.id]
                                            },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        val groupImage = chat.participantProfileImages[chat.id]
                                        if (groupImage != null) {
                                            AsyncImage(
                                                model = groupImage,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    chat.participantNames[chat.id]?.take(1)?.uppercase() ?: "G",
                                                    style = MaterialTheme.typography.displayMedium
                                                )
                                            }
                                        }
                                    }
                                    if (isMentor) {
                                        IconButton(
                                            onClick = { imagePicker.launch("image/*") },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = "Change Photo",
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = chat.participantNames[chat.id] ?: "Group Name",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (isMentor) {
                                        IconButton(onClick = { showEditNameDialog = true }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Name",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = chat.groupDescription ?: "No description",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isMentor) {
                                        IconButton(onClick = { showEditDescDialog = true }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Description",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Settings Section (Mentor Only)
                    if (isMentor) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        "Group Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Restrict Messaging",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "Only the mentor will be allowed to send messages in the group.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = chat.isOnlyMentorMessaging,
                                            onCheckedChange = { viewModel.updateGroupSettings(isOnlyMentorMessaging = it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Member List Header
                    item {
                        Text(
                            "Members (${chat.participants.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                        )
                    }

                    // Member List
                    items(chat.participants) { userId ->
                        val name = chat.participantNames[userId] ?: "Unknown User"
                        val imageUrl = chat.participantProfileImages[userId]
                        val isRestricted = chat.restrictedParticipants.contains(userId)
                        val isMemberMentor = chat.creatorId == userId

                        MemberCard(
                            name = name,
                            imageUrl = imageUrl,
                            isRestricted = isRestricted,
                            isMentor = isMemberMentor,
                            showActions = isMentor && userId != uiState.currentUser?.userId,
                            onRestrict = { viewModel.restrictMember(userId) },
                            onUnrestrict = { viewModel.unrestrictMember(userId) },
                            onSendMessage = { onMemberClick(userId) },
                            onShowImage = { selectedImageUrl = imageUrl }
                        )
                    }

                    // Leave Group Button
                    item {
                        Button(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.1f),
                                contentColor = Color.Red
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Leave Group", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Dialogs (unchanged but contextually fits)
    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(chat?.participantNames?.get(chat.id) ?: "") }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Group Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Group Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGroupSettings(name = newName)
                    showEditNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showEditDescDialog) {
        var newDesc by remember { mutableStateOf(chat?.groupDescription ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDescDialog = false },
            title = { Text("Edit Description") },
            text = {
                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { newDesc = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGroupSettings(description = newDesc)
                    showEditDescDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDescDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group?") },
            text = { Text("Are you sure you want to leave this group chat? You will no longer receive updates from this course group.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.leaveGroup()
                    showLeaveDialog = false
                }) { Text("Leave", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    selectedImageUrl?.let { url ->
        Dialog(onDismissRequest = { selectedImageUrl = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp)),
                color = Color.Black
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun MemberCard(
    name: String,
    imageUrl: String?,
    isRestricted: Boolean,
    isMentor: Boolean,
    showActions: Boolean,
    onRestrict: () -> Unit,
    onUnrestrict: () -> Unit,
    onSendMessage: () -> Unit,
    onShowImage: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onShowImage() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isRestricted) Color.Red else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isMentor) FontWeight.Bold else FontWeight.Medium
                )
                if (isMentor || isRestricted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isMentor) {
                            Badge(
                                containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
                                contentColor = Color(0xFF9C27B0)
                            ) {
                                Text("Mentor", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (isRestricted) {
                            Badge(
                                containerColor = Color.Red.copy(alpha = 0.1f),
                                contentColor = Color.Red
                            ) {
                                Text("Restricted", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            if (showActions) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Show Profile Image") },
                            onClick = {
                                showMenu = false
                                onShowImage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Send Message") },
                            onClick = {
                                showMenu = false
                                onSendMessage()
                            }
                        )
                        if (isRestricted) {
                            DropdownMenuItem(
                                text = { Text("Unrestrict Activity") },
                                onClick = {
                                    showMenu = false
                                    onUnrestrict()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Restrict Activity",
                                        color = Color.Red
                                    ) 
                                },
                                onClick = {
                                    showMenu = false
                                    onRestrict()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
