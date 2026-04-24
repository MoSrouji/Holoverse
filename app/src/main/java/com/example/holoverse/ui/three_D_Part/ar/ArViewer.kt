package com.example.holoverse.ui.three_D_Part.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.google.ar.core.Plane
import com.google.ar.core.Frame
import com.google.ar.core.Point
import com.google.ar.core.InstantPlacementPoint
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.HitResultNode
import io.github.sceneview.node.CubeNode
import java.util.EnumSet
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.ar.ARScene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.model.ModelInstance
import java.util.concurrent.atomic.AtomicReference

@Composable
fun ArViewer(
    modelPath: String?,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    scale: Float = 1f,
    isLoading: Boolean = false
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val currentFrame = remember { AtomicReference<Frame?>(null) }
    var trackingState by remember { mutableStateOf(TrackingState.PAUSED) }
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var modelNodeInstance by remember { mutableStateOf<ModelNode?>(null) }
    var baseScale by remember { mutableStateOf<Scale?>(null) }

    LaunchedEffect(modelNodeInstance, baseScale, scale, rotation) {
        val node = modelNodeInstance
        val base = baseScale
        if (node != null) {
            if (base != null) {
                node.scale = Scale(base.x * scale, base.y * scale, base.z * scale)
            }
            node.rotation = Rotation(y = rotation)
        }
    }

    var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var isLoadingModel by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelPath, anchor) {
        if (modelPath == null) return@LaunchedEffect

        modelInstance = null
        modelNodeInstance = null
        baseScale = null
        isLoadingModel = true
        loadError = null

        try {
            val instance = modelLoader.loadModelInstance(modelPath)
            if (instance == null) {
                loadError = "Failed to load model"
            } else {
                modelInstance = instance
            }
        } catch (e: Exception) {
            loadError = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoadingModel = false
        }
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = true,
            isOpaque = false,
            sessionCameraConfig = { session ->
                val filter = CameraConfigFilter(session)
                    .setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_60, CameraConfig.TargetFps.TARGET_FPS_30))
                session.getSupportedCameraConfigs(filter).firstOrNull() ?: session.cameraConfig
            },
            sessionConfiguration = { session, config ->
                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.focusMode = Config.FocusMode.AUTO

                // Enable Raw Depth for better accuracy if supported
                if (session.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY)) {
                    config.depthMode = Config.DepthMode.RAW_DEPTH_ONLY
                }
            },
            onSessionUpdated = { _, frame ->
                currentFrame.set(frame)
                if (trackingState != frame.camera.trackingState) {
                    trackingState = frame.camera.trackingState
                }
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { e, _ ->
                    val frame = currentFrame.get()
                    if (frame != null && frame.camera.trackingState == TrackingState.TRACKING) {
                        val hit = frame.hitTest(e.x, e.y).firstOrNull { hitResult ->
                            when (val trackable = hitResult.trackable) {
                                is Plane -> trackable.isPoseInPolygon(hitResult.hitPose) && trackable.trackingState == TrackingState.TRACKING
                                is Point -> trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL && trackable.trackingState == TrackingState.TRACKING
                                is InstantPlacementPoint -> true
                                else -> false
                            }
                        }

                        if (hit != null) {
                            anchor?.detach()
                            // Keep the model instance to avoid reloading
                            anchor = hit.createAnchorOrNull()
                        }
                    }
                }
            )
        ) {
            if (anchor == null && trackingState == TrackingState.TRACKING) {
                HitResultNode(
                    xPx = widthPx / 2f,
                    yPx = heightPx / 2f,
                    planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING, Plane.Type.VERTICAL),
                    instantPlacementPoint = true
                ) {
                    CubeNode(
                        engine = engine,
                        size = Scale(0.02f),
                        center = Position(0f, 0f, 0f)
                    )
                }
            }

            anchor?.let { currentAnchor ->
                key(currentAnchor) {
                    AnchorNode(anchor = currentAnchor) {
                        modelInstance?.let { instance ->
                            ModelNode(
                                modelInstance = instance,
                                scaleToUnits = 0.5f,
                                centerOrigin = Position(x = 0f, y = 0f, z = 0f),
                                isEditable = false,
                                apply = {
                                    modelNodeInstance = this
                                    baseScale = this.scale
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isLoadingModel) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        loadError?.let { error ->
            Surface(
                color = Color.Red.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Load Error: $error",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            val instruction = when (trackingState) {
                TrackingState.TRACKING -> "Tap on a surface to place the model"
                TrackingState.PAUSED -> "Move your phone to scan the area"
                else -> "AR is initializing..."
            }
            Text(
                text = instruction,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
