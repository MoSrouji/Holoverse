package com.example.holoverse.ui.three_D_Part.ar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.three_d_model.data.local.ModelCacheManager
import com.example.holoverse.ui.three_D_Part.ModelViewModel
import com.example.holoverse.ui.three_D_Part.gallery.ModelGalleryOverlay
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArScreen(
    appNavigator: AppNavigator,
    viewModel: ModelViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cacheManager = viewModel.cacheManager

    var cachedModelPath by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    val arStatus = rememberArStatus()
    var userRequestedInstall by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // File picker for local 3D models
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val name = it.lastPathSegment?.substringAfterLast('/') ?: "Local Model"
            viewModel.addLocalModel(name = name, path = it.toString())
        }
    }

    // Cache or download model whenever a new model is selected
    LaunchedEffect(uiState.selectedModel) {
        val model = uiState.selectedModel
        if (model != null) {
            isDownloading = true
            downloadError = null
            try {
                cachedModelPath = withContext(Dispatchers.IO) {
                    cacheManager.getModelPath(model.id, model.path)
                }
            } catch (e: Exception) {
                downloadError = "Failed to load model: ${e.localizedMessage}"
                cachedModelPath = null
            } finally {
                isDownloading = false
            }
        } else {
            cachedModelPath = null
        }
    }

    // Show download errors in a snackbar
    LaunchedEffect(downloadError) {
        downloadError?.let {
            snackbarHostState.showSnackbar(it)
            downloadError = null // reset after showing
        }
    }

    // ARCore install prompt
    if (arStatus == ArStatus.SUPPORTED_NOT_INSTALLED && !userRequestedInstall) {
        LaunchedEffect(Unit) {
            try {
                ArCoreApk.getInstance()
                    .requestInstall(context as android.app.Activity, true)
                userRequestedInstall = true
            } catch (e: UnavailableException) {
                // User declined or installation not possible – exit gracefully
                appNavigator.popBackStack()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // AR View – shown even when model path is null
            ArViewer(
                modelPath = cachedModelPath,
                modifier = Modifier.fillMaxSize(),
                rotation = uiState.modelRotation,
                scale = uiState.modelScale,
                isLoading = isDownloading
            )

            // Loading overlay
            if (isDownloading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading model...",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // AR controls and model gallery only when a valid model is ready
            if (cachedModelPath != null) {
                // Left-side control panel
                ArControlPanel(
                    rotation = uiState.modelRotation,
                    scale = uiState.modelScale,
                    onRotationChange = { viewModel.updateRotation(it) },
                    onScaleChange = { viewModel.updateScale(it) },
                    onReset = { viewModel.resetTransformations() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                )

                // Gallery overlay or toggle button
                if (uiState.showModelGallery) {
                    ModelGalleryOverlay(
                        models = uiState.models,
                        selectedModel = uiState.selectedModel,
                        downloadProgress = uiState.downloadProgress,
                        onModelSelected = { viewModel.selectModel(it) },
                        onAddLocalModel = {
                            filePickerLauncher.launch(arrayOf("model/*", "application/octet-stream"))
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(WindowInsets.statusBars.asPaddingValues())
                    )

                    // Close gallery button
                    IconButton(
                        onClick = { viewModel.setShowModelGallery(false) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 140.dp, end = 16.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Hide Gallery")
                    }
                } else {
                    // Show gallery button
                    Button(
                        onClick = { viewModel.setShowModelGallery(true) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Show Models")
                    }
                }
            } else if (!isDownloading && arStatus == ArStatus.SUPPORTED) {
                // Placeholder when no model is selected
                Text(
                    text = "No model selected",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Top back / switch button
            ExtendedFloatingActionButton(
                onClick = { appNavigator.popBackStack() },
                icon = { Icon(Icons.Rounded.ArrowBack, null) },
                text = { Text("Back") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 16.dp, end = 16.dp)
            )
        }
    }
}