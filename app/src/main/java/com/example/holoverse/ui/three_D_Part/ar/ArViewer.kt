package com.example.holoverse.ui.three_D_Part.ar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberARView
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumSet
import java.util.concurrent.atomic.AtomicReference

/**
 * Performance-optimized AR viewer designed for stability and "lightness".
 * Optimized for simultaneous use with video calls by capping FPS and simplifying CV tasks.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArViewer(
    modelPath: String?,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    scale: Float = 1f,
    isLoading: Boolean = false,
    mirrorSurface: android.view.Surface? = null
) {
    val engine = rememberEngine()
    val renderer = rememberRenderer(engine)
    val view = rememberARView(engine)
    val modelLoader = rememberModelLoader(engine)

    val currentFrame = remember { AtomicReference<Frame?>(null) }
    var trackingState by remember { mutableStateOf(TrackingState.PAUSED) }
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var modelNodeInstance by remember { mutableStateOf<ModelNode?>(null) }
    var baseScale by remember { mutableStateOf<Scale?>(null) }
    var surfaceDetectionQuality by remember { mutableStateOf(SurfaceDetectionQuality.SCANNING) }

    // Stability smoothing to prevent flickering labels
    var surfaceStabilityCount by remember { mutableIntStateOf(0) }
    val requiredStabilityFrames = 10

    // Frame processing throttle: 33ms (30fps) is ideal for logic vs performance balance
    var lastFrameProcessTime by remember { mutableLongStateOf(0L) }
    val frameProcessInterval = 33L

    // Mirroring SwapChain for WebRTC
    val mirrorSwapChain = remember(mirrorSurface) {
        mirrorSurface?.let { engine.createSwapChain(it) }
    }
    DisposableEffect(mirrorSwapChain) {
        onDispose {
            mirrorSwapChain?.let { engine.destroySwapChain(it) }
        }
    }

    // Memory Leak Safeguard: Detach anchors and clear frame references on dispose
    DisposableEffect(Unit) {
        onDispose {
            anchor?.detach()
            anchor = null
            currentFrame.set(null)
            modelNodeInstance = null
        }
    }

    LaunchedEffect(modelNodeInstance, baseScale, scale, rotation) {
        val node = modelNodeInstance ?: return@LaunchedEffect
        val base = baseScale ?: return@LaunchedEffect
        node.scale = Scale(base.x * scale, base.y * scale, base.z * scale)
        node.rotation = Rotation(y = rotation)
    }

    var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var isLoadingModel by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelPath) {
        if (modelPath == null) return@LaunchedEffect
        isLoadingModel = true
        try {
            val instance = withContext(Dispatchers.IO) {
                modelLoader.loadModelInstance(modelPath)
            }
            modelInstance = instance
        } catch (e: Exception) {
            loadError = e.localizedMessage
        } finally {
            isLoadingModel = false
        }
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            renderer = renderer,
            view = view,
            modelLoader = modelLoader,
            planeRenderer = true,
            sessionCameraConfig = { session ->
                val filter = CameraConfigFilter(session).apply {
                    // FORCE 30 FPS: This is critical for keeping the phone cool during video calls
                    setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30))
                    // Prefer depth but allow fallback to ensure 100% device compatibility
                    setDepthSensorUsage(EnumSet.of(
                        CameraConfig.DepthSensorUsage.DO_NOT_USE,
                        CameraConfig.DepthSensorUsage.REQUIRE_AND_USE
                    ))
                }
                session.getSupportedCameraConfigs(filter).firstOrNull() ?: session.cameraConfig
            },
            sessionConfiguration = { session, config ->
                config.apply {
                    // AUTOMATIC Depth is smoother and more accurate for stable placement than RAW
                    depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        Config.DepthMode.AUTOMATIC
                    } else Config.DepthMode.DISABLED

                    // Enable both horizontal and vertical to help tracking stability
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    // Use HDR light estimation for more realistic lighting and reflections
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    focusMode = Config.FocusMode.AUTO
                }
            },
            onSessionUpdated = { session, frame ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFrameProcessTime >= frameProcessInterval) {
                    lastFrameProcessTime = currentTime
                    currentFrame.set(frame)
                    trackingState = frame.camera.trackingState

                    val hasSurface = hasGoodSurfaceForPlacement(session)

                    // Temporal smoothing logic: require multiple stable frames before green-lighting
                    if (hasSurface) {
                        if (surfaceStabilityCount < requiredStabilityFrames) surfaceStabilityCount++
                    } else {
                        if (surfaceStabilityCount > 0) surfaceStabilityCount--
                    }

                    surfaceDetectionQuality = when {
                        trackingState != TrackingState.TRACKING -> SurfaceDetectionQuality.SCANNING
                        surfaceStabilityCount >= requiredStabilityFrames -> SurfaceDetectionQuality.EXCELLENT
                        surfaceStabilityCount > 0 -> SurfaceDetectionQuality.DETECTING
                        else -> SurfaceDetectionQuality.SCANNING
                    }

                    // Mirroring logic
                    mirrorSwapChain?.let { sc ->
                        if (renderer.beginFrame(sc, System.nanoTime())) {
                            renderer.render(view)
                            renderer.endFrame()
                        }
                    }
                }
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, _ ->
                    val frame = currentFrame.get() ?: return@rememberOnGestureListener
                    if (trackingState != TrackingState.TRACKING) return@rememberOnGestureListener

                    // Optimized hit testing preferring stable planes
                    val bestHit = findBestSurfaceHit(frame, motionEvent.x, motionEvent.y)
                    if (bestHit != null) {
                        anchor?.detach()
                        anchor = bestHit.createAnchorOrNull()
                    }
                }
            )
        ) {
            // Placement indicator
            if (anchor == null && trackingState == TrackingState.TRACKING) {
                HitResultNode(
                    xPx = widthPx / 2f,
                    yPx = heightPx / 2f,
                    planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING),
                    instantPlacementPoint = true
                ) {
                    CubeNode(engine = engine, size = Scale(0.01f), center = Position(0f, 0f, 0f))
                }
            }

            // The Model
            anchor?.let { currentAnchor ->
                key(currentAnchor) {
                    AnchorNode(anchor = currentAnchor) {
                        modelInstance?.let { instance ->
                            ModelNode(
                                modelInstance = instance,
                                scaleToUnits = 0.5f,
                                centerOrigin = Position(0f, 0f, 0f),
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

        // Feedback UI
        if (isLoadingModel || isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Error display
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
            color = Color.Black.copy(alpha = 0.6f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            AnimatedContent(
                targetState = surfaceDetectionQuality,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "StatusText"
            ) { quality ->
                val statusText = when (quality) {
                    SurfaceDetectionQuality.EXCELLENT -> "✓ Ready to place"
                    SurfaceDetectionQuality.DETECTING -> "Detecting floor..."
                    SurfaceDetectionQuality.SCANNING -> "Move phone slowly"
                }
                Text(
                    text = statusText,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun findBestSurfaceHit(frame: Frame, x: Float, y: Float): HitResult? {
    // Strategy: Prefer Persistent Planes for 100% stability
    val planeHit = frame.hitTest(x, y).firstOrNull { hit ->
        val trackable = hit.trackable
        trackable is Plane &&
        trackable.isPoseInPolygon(hit.hitPose) &&
        trackable.trackingState == TrackingState.TRACKING
    }
    if (planeHit != null) return planeHit

    // Fallback to depth-based hit test
    return frame.hitTest(x, y).firstOrNull { hit ->
        hit.trackable.trackingState == TrackingState.TRACKING
    }
}

private fun hasGoodSurfaceForPlacement(session: Session): Boolean {
    // Check all trackables instead of just "updated" ones to prevent flickering when stationary
    return session.getAllTrackables(Plane::class.java).any { plane ->
        plane.trackingState == TrackingState.TRACKING &&
        plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
        plane.subsumedBy == null
    }
}

private enum class SurfaceDetectionQuality { SCANNING, DETECTING, EXCELLENT }
