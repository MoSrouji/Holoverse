package com.example.holoverse.ui.three_D_Part.ar

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.threedmodel.presentation.ModelViewModel
import com.example.holoverse.threedmodel.presentation.ar.rememberArStatus
import com.example.holoverse.ui.three_D_Part.gallery.ModelGalleryOverlay
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableException
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArScreen(
    appNavigator: AppNavigator,
    viewModel: ModelViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    // Show download errors in a snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // ARCore install prompt
    if (arStatus == ArStatus.SUPPORTED_NOT_INSTALLED && !userRequestedInstall) {
        LaunchedEffect(Unit) {
            try {
                ArCoreApk.getInstance()
                    .requestInstall(context as Activity, true)
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
                modelPath = uiState.selectedModelPath,
                modifier = Modifier.fillMaxSize(),
                rotation = uiState.modelRotation,
                verticalRotation = uiState.modelVerticalRotation,
                scale = uiState.modelScale,
                isLoading = uiState.selectedModelPath == null && uiState.selectedModel != null
            )

            // Loading overlay
            if (uiState.selectedModelPath == null && uiState.selectedModel != null) {
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
            if (uiState.selectedModelPath != null) {
                // Left-side control panel
                ArControlPanel(
                    rotation = uiState.modelRotation,
                    verticalRotation = uiState.modelVerticalRotation,
                    scale = uiState.modelScale,
                    onRotationChange = { viewModel.updateRotation(it) },
                    onVerticalRotationChange = { viewModel.updateVerticalRotation(it) },
                    onScaleChange = { viewModel.updateScale(it) },
                    onReset = { viewModel.resetTransformations() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp)
                )

                // Gallery overlay or toggle button
                if (uiState.showModelGallery) {
                    ModelGalleryOverlay(
                        models = uiState.models,
                        selectedModel = uiState.selectedModel,
                        downloadProgress = uiState.downloadProgress,
                        onModelSelected = { viewModel.selectModel(it) },
                        onAddLocalModel = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "model/*",
                                    "application/octet-stream"
                                )
                            )
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
                            .padding(bottom = 150.dp, end = 24.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Hide Gallery")
                    }
                } else {
                    // Show gallery button
                    Surface(
                        onClick = { viewModel.setShowModelGallery(true) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.GridView, contentDescription = null)
                            Text(
                                "Models",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (uiState.selectedModel == null && arStatus == ArStatus.SUPPORTED) {
                // Placeholder when no model is selected
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ViewInAr,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Pick a model to start",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.setShowModelGallery(true) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Open Gallery")
                    }
                }
            }

            // Top back button - TopStart for standard navigation
            IconButton(
                onClick = { appNavigator.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    }
}