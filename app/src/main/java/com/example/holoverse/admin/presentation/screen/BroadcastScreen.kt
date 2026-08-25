package com.example.holoverse.admin.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.admin.presentation.viewmodel.BroadcastViewModel
import com.example.holoverse.notifications.domain.repository.BroadcastTarget
import com.example.holoverse.core.ui.spatial.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastScreen(
    onBackClick: () -> Unit,
    darkTheme: Boolean,
    viewModel: BroadcastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val headerBrush = remember(darkTheme) { Brush(darkTheme) }
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Broadcast sent successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Broadcast", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Target Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Target Audience",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BroadcastTarget.entries.forEachIndexed { index, target ->
                        SegmentedButton(
                            selected = uiState.target == target,
                            onClick = { viewModel.onTargetChange(target) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = BroadcastTarget.entries.size
                            )
                        ) {
                            Text(target.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            // Title Input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notification Title") },
                placeholder = { Text("e.g., System Update") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Message Input
            OutlinedTextField(
                value = uiState.message,
                onValueChange = viewModel::onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text("Notification Message") },
                placeholder = { Text("Write your message here...") },
                shape = RoundedCornerShape(16.dp)
            )

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading && uiState.title.isNotBlank() && uiState.message.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Broadcast", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Broadcast") },
            text = { Text("This will send a push notification to all ${uiState.target.name.lowercase()} users. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.sendBroadcast()
                    }
                ) {
                    Text("Send Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
