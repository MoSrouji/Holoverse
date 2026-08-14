package com.example.holoverse.webrtc.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage

@Composable
fun OutgoingCallScreen(
    callId: String,
    roomId: String,
    receiverName: String,
    receiverImageUrl: String?,
    viewModel: VideoCallViewModel = hiltViewModel(),
    onCallConnected: () -> Unit,
    onEndCall: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState(initial = null)

    LaunchedEffect(callId, roomId) {
        viewModel.initCall(roomId = roomId, inviteId = callId, isOffer = true)
    }

    LaunchedEffect(connectionState) {
        if (connectionState == "CONNECTED") {
            onCallConnected()
        }
    }

    BackHandler {
        viewModel.endCall()
        onEndCall()
    }

    OutgoingCallContent(
        receiverName = receiverName,
        receiverImageUrl = receiverImageUrl,
        connectionState = connectionState,
        onEndCall = {
            viewModel.endCall()
            onEndCall()
        }
    )
}

@Composable
fun OutgoingCallContent(
    receiverName: String,
    receiverImageUrl: String?,
    connectionState: String?,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        // Receiver Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = receiverImageUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Receiver Avatar",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = receiverName,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (connectionState) {
                    "CONNECTING" -> "Connecting..."
                    "CONNECTED" -> "Connected"
                    else -> "Calling..."
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
        }

        // Action Button Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "End Call", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OutgoingCallScreenPreview() {
    OutgoingCallContent(
        receiverName = "John Smith",
        receiverImageUrl = null,
        connectionState = "CONNECTING",
        onEndCall = {}
    )
}
