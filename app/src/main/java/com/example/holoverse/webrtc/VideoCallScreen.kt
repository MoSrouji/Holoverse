package com.example.holoverse.webrtc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    callId: String,
    isOffer: Boolean,
    viewModel: VideoCallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    )

    if (permissionsState.allPermissionsGranted) {
        VideoCallContent(callId, isOffer, viewModel, onCallEnded)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FloatingActionButton(
                onClick = { permissionsState.launchMultiplePermissionRequest() }
            ) {
                Text(text = "Grant Permissions", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun VideoCallContent(
    callId: String,
    isOffer: Boolean,
    viewModel: VideoCallViewModel,
    onCallEnded: () -> Unit
) {
    val localTrack by viewModel.localVideoTrack.collectAsState()
    val remoteTrack by viewModel.remoteVideoTrack.collectAsState()
    val eglContext = (viewModel as? VideoCallViewModel)?.getEglContext()

    LaunchedEffect(callId) {
        viewModel.initCall(callId, isOffer)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote Video
        remoteTrack?.let { track ->
            VideoRenderer(
                track = track,
                eglContext = eglContext,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Local Video (Small overlay)
        localTrack?.let { track ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
            ) {
                VideoRenderer(
                    track = track,
                    eglContext = eglContext,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Hang up button
        FloatingActionButton(
            onClick = {
                viewModel.endCall()
                onCallEnded()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = Color.Red
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White
            )
        }
    }
}

@Composable
fun VideoRenderer(
    track: VideoTrack,
    eglContext: EglBase.Context?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setMirror(true)
                track.addSink(this)
            }
        },
        modifier = modifier,
        update = { view ->
            // Update logic if needed
        },
        onRelease = { view ->
            track.removeSink(view)
            view.release()
        }
    )
}
