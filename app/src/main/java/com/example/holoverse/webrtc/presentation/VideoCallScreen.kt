package com.example.holoverse.webrtc.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.threedmodel.presentation.ModelViewModel
import com.example.holoverse.threedmodel.presentation.ar.rememberArStatus
import com.example.holoverse.ui.three_D_Part.ar.ArStatus
import com.example.holoverse.ui.three_D_Part.ar.ArViewer
import com.example.holoverse.ui.three_D_Part.gallery.ModelGalleryOverlay
import com.example.holoverse.webrtc.domain.model.CallMode
import com.example.holoverse.whiteboard.presentation.WhiteboardManager
import com.example.holoverse.whiteboard.presentation.WhiteboardToolbar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoTrackView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.RendererCommon
import livekit.org.webrtc.SurfaceViewRenderer
import kotlin.math.roundToInt



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    callId: String,
    roomId: String,
    isOffer: Boolean,
    viewModel: VideoCallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(android.Manifest.permission.CAMERA)
            add(android.Manifest.permission.RECORD_AUDIO)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    )

    if (permissionsState.allPermissionsGranted) {
        VideoCallContent(callId, roomId, isOffer, viewModel, onCallEnded)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionsState.shouldShowRationale) {
                "Camera and Microphone permissions are needed for video calls."
            } else {
                "Permissions denied. Please enable them in app settings."
            }
            Text(text = textToShow, modifier = Modifier.padding(16.dp))
            FloatingActionButton(
                onClick = {
                    if (permissionsState.shouldShowRationale || !permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
            ) {
                Text(text = "Grant Permissions", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun VideoCallContent(
    callId: String,
    roomId: String,
    isOffer: Boolean,
    viewModel: VideoCallViewModel,
    onCallEnded: () -> Unit,
    modelViewModel: ModelViewModel = hiltViewModel()
) {
    val localTrack by viewModel.localVideoTrack.collectAsState()
    val remoteTrack by viewModel.remoteVideoTrack.collectAsState()
    val remoteParticipants by viewModel.remoteParticipants.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState(initial = null)
    val isCallEnded by viewModel.isCallEnded.collectAsState()
    val isPdfEnabled by viewModel.isPdfEnabled.collectAsState()
    val callMode by viewModel.callMode.collectAsState()
    val pdfBitmap by viewModel.pdfBitmap.collectAsState()
    val arMirrorSurface by viewModel.arMirrorSurface.collectAsState()
    val syntheticResolution by viewModel.syntheticResolution.collectAsState()
    val room by viewModel.room.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isChatVisible by viewModel.isChatVisible.collectAsState()
    val isMessagingRestricted by viewModel.isMessagingRestricted.collectAsState()
    val currentUserIsMentor by viewModel.currentUserIsMentor.collectAsState()
    val isGridViewEnabled by viewModel.isGridViewEnabled.collectAsState()
    val mentorId by viewModel.mentorId.collectAsState()
    val eglContext = viewModel.getEglContext()

    val arStatus = rememberArStatus()
    val whiteboardManager = remember { WhiteboardManager() }

    LaunchedEffect(whiteboardManager) {
        viewModel.setWhiteboardManager(whiteboardManager)
    }

    // PDF Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.loadPdf(it)
            viewModel.setCallMode(CallMode.PDF)
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var isCameraEnabled by remember { mutableStateOf(true) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    val modelUiState by modelViewModel.uiState.collectAsState()
    val cacheManager = modelViewModel.cacheManager
    var cachedModelPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelUiState.selectedModel) {
        val model = modelUiState.selectedModel
        if (model != null) {
            try {
                cachedModelPath = withContext(Dispatchers.IO) {
                    cacheManager.getModelPath(model.id, model.path)
                }
            } catch (e: Exception) {
                cachedModelPath = null
            }
        }
    }

    LaunchedEffect(isCallEnded) {
        if (isCallEnded) {
            onCallEnded()
        }
    }

    LaunchedEffect(callId, roomId) {
        viewModel.initCall(
            roomId = roomId,
            inviteId = callId,
            isOffer = isOffer
        ) // Explicit boolean to be sure
    }

    BackHandler {
        viewModel.endCall()
        onCallEnded()
    }

    RoomScope(
        passedRoom = room,
        connect = false,
        disconnectOnDispose = false
    ) { lkRoom ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val density = LocalDensity.current

            // Main View (Remote Video, Whiteboard, or PDF)
            val mentorParticipant = remoteParticipants.find { it.identity == mentorId }
            val mentorTrack = mentorParticipant?.livekitVideoTrack

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                if (currentUserIsMentor && !isGridViewEnabled || !currentUserIsMentor) {
                    if (!currentUserIsMentor) {
                        // Student View: Always focus on Mentor
                        if (mentorParticipant != null) {
                            if (mentorTrack != null && mentorParticipant.isVideoEnabled) {
                                VideoRenderer(
                                    track = mentorTrack,
                                    eglContext = eglContext,
                                    modifier = Modifier.fillMaxSize(),
                                    scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT
                                )
                            } else {
                                CameraDisabledPlaceholder(modifier = Modifier.fillMaxSize())
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Waiting for mentor...", color = Color.White)
                            }
                        }
                    } else {
                        // Mentor Focus Mode: See own content
                        when (callMode) {
                            CallMode.WHITEBOARD -> {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                                    WhiteboardView(whiteboardManager)
                                }
                            }

                            CallMode.PDF -> {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                                    // Draw PDF background locally
                                    pdfBitmap?.let { bitmap ->
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "PDF Page",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Draw Whiteboard overlay on top of PDF
                                    WhiteboardView(whiteboardManager)

                                    // PDF Controls (Overlay)
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(onClick = { viewModel.pdfPreviousPage() }) {
                                            Icon(
                                                Icons.Default.ChevronLeft,
                                                contentDescription = "Previous Page"
                                            )
                                        }
                                        IconButton(onClick = { viewModel.pdfNextPage() }) {
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = "Next Page"
                                            )
                                        }
                                    }
                                }
                            }

                            CallMode.AR -> {
                                ArViewer(
                                    modelPath = cachedModelPath,
                                    modifier = Modifier.fillMaxSize(),
                                    rotation = modelUiState.modelRotation,
                                    scale = modelUiState.modelScale,
                                    mirrorSurface = arMirrorSurface,
                                    mirrorResolution = syntheticResolution
                                )
                            }

                            else -> {
                                if (localTrack != null && isCameraEnabled) {
                                    VideoRenderer(
                                        track = localTrack!!,
                                        eglContext = eglContext,
                                        modifier = Modifier.fillMaxSize(),
                                        isMirror = true,
                                        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT
                                    )
                                } else {
                                    CameraDisabledPlaceholder(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                } else {
                    // Mentor Grid Mode: See all students
                    if (remoteParticipants.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(if (remoteParticipants.size > 1) 2 else 1),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(remoteParticipants) { participant ->
                                val lkTrack = participant.livekitVideoTrack
                                if (lkTrack != null && participant.isVideoEnabled) {
                                    VideoRenderer(
                                        track = lkTrack,
                                        eglContext = eglContext,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (remoteParticipants.size > 2) 200.dp else 400.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT
                                    )
                                } else {
                                    CameraDisabledPlaceholder(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (remoteParticipants.size > 2) 200.dp else 400.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        iconSize = 32.dp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Waiting for students...", color = Color.White)
                        }
                    }
                }
            }

            val isArMode = callMode == CallMode.AR

            // In-Call Chat Overlay (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .width(260.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedVisibility(
                        visible = isChatVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header with Sender name and Eye Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "In-Call Chat",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.toggleChatVisibility() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Hide Chat",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Message List
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    messages.forEach { message ->
                                        Column {
                                            Text(
                                                text = message.senderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Cyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = message.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (messages.isEmpty()) {
                                        Text(
                                            "No messages yet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Input or Restricted Label
                                if (!isMessagingRestricted) {
                                    var chatText by remember { mutableStateOf("") }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color.White.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = chatText,
                                            onValueChange = { chatText = it },
                                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White
                                            ),
                                            modifier = Modifier.weight(1f),
                                            cursorBrush = androidx.compose.ui.graphics.SolidColor(
                                                Color.White
                                            )
                                        )
                                        IconButton(
                                            onClick = {
                                                if (chatText.isNotBlank()) {
                                                    viewModel.sendMessage(chatText)
                                                    chatText = ""
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "Messaging restricted by owner",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Show button when hidden
                    if (!isChatVisible) {
                        IconButton(
                            onClick = { viewModel.toggleChatVisibility() },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Show Chat",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Connection State overlay
            if (!isArMode) {
                connectionState?.let { state ->
                    GlassySurface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Status: $state",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Local Video / Remote Video Overlay (DRAGGABLE)
            val showOverlay = if (currentUserIsMentor) isGridViewEnabled else true

            if (showOverlay) {
                DraggableVideoOverlay(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp),
                    screenSize = screenWidth to screenHeight
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.DarkGray)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    ) {
                        if (callMode == CallMode.WHITEBOARD || callMode == CallMode.PDF || callMode == CallMode.AR || !currentUserIsMentor) {
                            // When whiteboard, PDF or AR is full screen, show remote video in the small overlay
                            // For students, they might want to see themselves even if mentor is full screen
                            if (currentUserIsMentor) {
                                val studentParticipant = remoteParticipants.firstOrNull()
                                val overlayTrack = studentParticipant?.livekitVideoTrack
                                if (overlayTrack != null && studentParticipant.isVideoEnabled) {
                                    VideoTrackView(
                                        videoTrack = overlayTrack,
                                        modifier = Modifier.fillMaxSize(),
                                        mirror = false,
                                        scaleType = ScaleType.FitInside
                                    )
                                } else {
                                    CameraDisabledPlaceholder(
                                        modifier = Modifier.fillMaxSize(),
                                        iconSize = 24.dp
                                    )
                                }
                            } else {
                                if (localTrack != null && isCameraEnabled) {
                                    VideoTrackView(
                                        videoTrack = localTrack!!,
                                        modifier = Modifier.fillMaxSize(),
                                        mirror = true,
                                        scaleType = ScaleType.Fill
                                    )
                                } else {
                                    CameraDisabledPlaceholder(
                                        modifier = Modifier.fillMaxSize(),
                                        iconSize = 24.dp
                                    )
                                }
                            }
                        } else {
                            if (localTrack != null && isCameraEnabled) {
                                VideoTrackView(
                                    videoTrack = localTrack!!,
                                    modifier = Modifier.fillMaxSize(),
                                    mirror = true,
                                    scaleType = ScaleType.FitInside
                                )
                            } else {
                                CameraDisabledPlaceholder(
                                    modifier = Modifier.fillMaxSize(),
                                    iconSize = 24.dp
                                )
                            }
                        }
                    }
                }
            }

            // AR Tools Sidebar
            AnimatedVisibility(
                visible = isArMode && !modelUiState.showModelGallery,
                enter = fadeIn() + slideInVertically { h: Int -> h / 2 },
                exit = fadeOut() + slideOutVertically { h: Int -> h / 2 },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                ArToolsSidebar(
                    rotation = modelUiState.modelRotation,
                    scale = modelUiState.modelScale,
                    onRotationChange = { rot: Float -> modelViewModel.updateRotation(rot) },
                    onScaleChange = { sc: Float -> modelViewModel.updateScale(sc) },
                    onReset = { modelViewModel.resetTransformations() },
                    onToggleGallery = { modelViewModel.setShowModelGallery(true) }
                )
            }

            // Model Gallery Overlay (Integrated for AR)
            AnimatedVisibility(
                visible = isArMode && modelUiState.showModelGallery,
                enter = fadeIn() + slideInVertically { h: Int -> h },
                exit = fadeOut() + slideOutVertically { h: Int -> h },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Background scrim to close gallery
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { modelViewModel.setShowModelGallery(false) }
                    )

                    ModelGalleryOverlay(
                        models = modelUiState.models,
                        selectedModel = modelUiState.selectedModel,
                        downloadProgress = modelUiState.downloadProgress,
                        onModelSelected = { model: com.example.holoverse.threedmodel.domain.model.Model ->
                            modelViewModel.selectModel(
                                model
                            )
                        },
                        onAddLocalModel = { /* Handle local model add if needed */ },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    IconButton(
                        onClick = { modelViewModel.setShowModelGallery(false) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Gallery",
                            tint = Color.White
                        )
                    }
                }
            }

            // Controls (ENSURE THEY ARE ON TOP)
            Box(
                modifier = Modifier
                    .align(if (isArMode) Alignment.TopCenter else Alignment.BottomCenter)
                    .then(
                        if (isArMode) Modifier
                            .statusBarsPadding()
                            .padding(top = 16.dp)
                        else Modifier.padding(bottom = 48.dp) // Increased padding for better visibility
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isArMode) {
                        connectionState?.let { state ->
                            GlassySurface(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = "Status: $state",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    CallControlRow(
                        isMuted = isMuted,
                        isCameraEnabled = isCameraEnabled,
                        isSpeakerOn = isSpeakerOn,
                        callMode = callMode,
                        arStatus = arStatus,
                        onToggleMute = {
                            isMuted = !isMuted
                            viewModel.toggleMute(isMuted)
                        },
                        onToggleCamera = {
                            isCameraEnabled = !isCameraEnabled
                            viewModel.toggleCamera(isCameraEnabled)
                        },
                        onToggleSpeaker = {
                            isSpeakerOn = !isSpeakerOn
                            viewModel.toggleSpeaker(isSpeakerOn)
                        },
                        onSwitchCamera = { viewModel.switchCamera() },
                        onToggleWhiteboard = {
                            val nextMode =
                                if (callMode == CallMode.WHITEBOARD) CallMode.VIDEO else CallMode.WHITEBOARD
                            viewModel.setCallMode(nextMode)
                        },
                        onTogglePdf = {
                            if (callMode == CallMode.PDF) {
                                viewModel.setCallMode(CallMode.VIDEO)
                            } else {
                                pdfPickerLauncher.launch("application/pdf")
                            }
                        },
                        onToggleAr = {
                            val nextMode =
                                if (callMode == CallMode.AR) CallMode.VIDEO else CallMode.AR
                            viewModel.setCallMode(nextMode)
                        },
                        onToggleChatLock = {
                            viewModel.toggleMessagingRestriction()
                        },
                        isMessagingRestricted = isMessagingRestricted,
                        showChatLock = currentUserIsMentor,
                        isGridViewEnabled = isGridViewEnabled,
                        onToggleGridView = { viewModel.toggleGridView() },
                        showGridView = currentUserIsMentor,
                        onEndCall = {
                            viewModel.endCall()
                            onCallEnded()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassySurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = shape,
        modifier = modifier.blur(0.dp) // Real blur is expensive, using semi-transparent black for better performance during calls
    ) {
        content()
    }
}

@Composable
fun CallControlRow(
    isMuted: Boolean,
    isCameraEnabled: Boolean,
    isSpeakerOn: Boolean,
    callMode: CallMode,
    arStatus: ArStatus,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleWhiteboard: () -> Unit,
    onTogglePdf: () -> Unit,
    onToggleAr: () -> Unit,
    onToggleChatLock: () -> Unit = {},
    isMessagingRestricted: Boolean = false,
    showChatLock: Boolean = false,
    isGridViewEnabled: Boolean = false,
    onToggleGridView: () -> Unit = {},
    showGridView: Boolean = false,
    onEndCall: () -> Unit
) {
    val isArMode = callMode == CallMode.AR
    val scrollState = rememberScrollState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showChatLock) {
                ControlIcon(
                    icon = if (isMessagingRestricted) Icons.Default.SpeakerNotesOff else Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Toggle Chat Lock",
                    isActive = isMessagingRestricted,
                    activeColor = Color.Yellow,
                    onClick = onToggleChatLock
                )
            }

            if (showGridView) {
                ControlIcon(
                    icon = Icons.Default.GridView,
                    contentDescription = "Toggle Grid View",
                    isActive = isGridViewEnabled,
                    activeColor = Color.Cyan,
                    onClick = onToggleGridView
                )
            }

            ControlIcon(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                isActive = isMuted,
                activeColor = Color.Red,
                onClick = onToggleMute
            )

            ControlIcon(
                icon = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = "Camera",
                isActive = !isCameraEnabled,
                activeColor = Color.Red,
                onClick = onToggleCamera
            )

            if (!isArMode) {
                ControlIcon(
                    icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Speaker",
                    isActive = isSpeakerOn,
                    activeColor = Color.Green,
                    onClick = onToggleSpeaker
                )

                ControlIcon(
                    icon = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    onClick = onSwitchCamera
                )

                ControlIcon(
                    icon = Icons.Rounded.Gesture,
                    contentDescription = "Whiteboard",
                    isActive = callMode == CallMode.WHITEBOARD,
                    activeColor = Color.Magenta,
                    onClick = onToggleWhiteboard
                )

                ControlIcon(
                    icon = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    isActive = callMode == CallMode.PDF,
                    activeColor = Color.Red,
                    onClick = onTogglePdf
                )
            }

            if (arStatus == ArStatus.SUPPORTED) {
                ControlIcon(
                    icon = Icons.Rounded.ViewInAr,
                    contentDescription = "AR",
                    isActive = isArMode,
                    activeColor = Color.Blue,
                    onClick = onToggleAr
                )
            }

            FloatingActionButton(
                onClick = onEndCall,
                containerColor = Color.Red,
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End", tint = Color.White)
            }
        }

        if (scrollState.maxValue > 0) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(2) { index ->
                    val isActive = if (index == 0) {
                        scrollState.value < scrollState.maxValue / 2
                    } else {
                        scrollState.value >= scrollState.maxValue / 2
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color.White else Color.White.copy(
                                    alpha = 0.3f
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ControlIcon(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isActive) activeColor.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.4f),
                CircleShape
            ),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ArToolsSidebar(
    rotation: Float,
    scale: Float,
    onRotationChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onReset: () -> Unit,
    onToggleGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(64.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Rotation Slider (Vertical)
        ArToolSlider(
            value = rotation,
            onValueChange = onRotationChange,
            valueRange = 0f..360f,
            icon = Icons.Default.RestartAlt,
            label = "Rotate"
        )

        // Scale Slider (Vertical)
        ArToolSlider(
            value = scale,
            onValueChange = onScaleChange,
            valueRange = 0.1f..3f,
            icon = Icons.Default.ChevronRight,
            label = "Scale"
        )

        IconButton(onClick = onToggleGallery) {
            Icon(Icons.Rounded.ViewInAr, contentDescription = "Gallery", tint = Color.White)
        }

        IconButton(onClick = onReset) {
            Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color.White)
        }
    }
}

@Composable
fun ArToolSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.Center) {
            // Slider doesn't support vertical orientation directly in M3 yet, 
            // but we can rotate it or use a custom component. For now, horizontal to keep it simple but compact.
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .width(100.dp)
                    .scale(0.8f)
                    .then(Modifier.offset(y = 0.dp)), // Could rotate here if needed
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun DraggableVideoOverlay(
    modifier: Modifier = Modifier,
    screenSize: Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp>,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(110.dp, 195.dp) // Slightly smaller and better aspect ratio
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        content()
    }
}

@Composable
fun WhiteboardView(whiteboardManager: WhiteboardManager, modifier: Modifier = Modifier) {
    var textInput by remember { mutableStateOf("") }
    val pendingPosition = whiteboardManager.pendingTextPosition

    if (pendingPosition != null) {
        AlertDialog(
            onDismissRequest = {
                whiteboardManager.pendingTextPosition = null
                textInput = ""
            },
            title = { Text("Add Text") },
            text = {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Type something...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (textInput.isNotBlank()) {
                        whiteboardManager.addText(textInput)
                    }
                    whiteboardManager.pendingTextPosition = null
                    textInput = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    whiteboardManager.pendingTextPosition = null
                    textInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> whiteboardManager.onTouchStart(offset) },
                    onDrag = { change, _ -> whiteboardManager.onTouchMove(change.position) },
                    onDragEnd = { whiteboardManager.onTouchEnd() },
                    onDragCancel = { whiteboardManager.onTouchEnd() }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            whiteboardManager.updateSourceSize(size)
            whiteboardManager.draw(this)
        }
        WhiteboardToolbar(
            manager = whiteboardManager,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}

@Composable
fun CameraDisabledPlaceholder(
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 48.dp
) {
    Box(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideocamOff,
            contentDescription = "Camera is off",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun VideoRenderer(
    track: io.livekit.android.room.track.VideoTrack,
    eglContext: EglBase.Context?,
    modifier: Modifier = Modifier,
    isMirror: Boolean = false,
    scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT
) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglContext, null)
                setScalingType(scalingType)
                setMirror(isMirror)
            }
        },
        modifier = modifier,
        update = { view ->
            val oldTrack = view.tag as? io.livekit.android.room.track.VideoTrack
            if (oldTrack != track) {
                oldTrack?.removeRenderer(view)
                view.tag = track
                track.addRenderer(view)
            }
        },
        onRelease = { view ->
            val oldTrack = view.tag as? io.livekit.android.room.track.VideoTrack
            oldTrack?.removeRenderer(view)
            view.release()
        }
    )
}

