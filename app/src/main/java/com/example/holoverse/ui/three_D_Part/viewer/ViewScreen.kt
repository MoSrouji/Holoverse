package com.example.holoverse.ui.three_D_Part.viewer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.three_D_Part.ModelViewModel
import com.example.holoverse.ui.three_D_Part.ar.ArStatus
import com.example.holoverse.ui.three_D_Part.ar.rememberArStatus
import com.example.holoverse.ui.three_D_Part.gallery.ModelGalleryOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ViewerScreen(
    appNavigator: AppNavigator,
    viewModel: ModelViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cacheManager = viewModel.cacheManager
    var cachedModelPath by remember { mutableStateOf<String?>(null) }
    val arStatus = rememberArStatus()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                // For local files, we can use the URI directly or copy it to app storage
                // For now, let's just pass the URI string. 
                // We might need to handle content URIs in ModelCacheManager
                viewModel.addLocalModel(
                    name = it.lastPathSegment?.substringAfterLast('/') ?: "Local Model",
                    path = it.toString()
                )
            }
        }
    )

    LaunchedEffect(uiState.selectedModel) {
        uiState.selectedModel?.let { model ->
            try {
                if (model.path.startsWith("content://") || model.path.startsWith("file://")) {
                    cachedModelPath = model.path
                } else {
                    cachedModelPath = cacheManager.getModelPath(model.id, model.path)
                }
            } catch (e: Exception) {
                // Handle the error gracefully without crashing
                cachedModelPath = null
                // We can potentially update the UI state with this error if needed
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.models.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    cachedModelPath?.let { path ->
                        if (path.startsWith("http") && !path.startsWith("file://") && !path.startsWith("content://")) {
                            // This means getModelPath is still downloading or failed
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            SceneViewer(modelPath = path, modifier = Modifier.fillMaxSize())
                        }
                    }

                    if (uiState.isLoading && uiState.selectedModel != null) {
                         CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                if (uiState.showModelGallery) {
                    ModelGalleryOverlay(
                        models = uiState.models,
                        selectedModel = uiState.selectedModel,
                        downloadProgress = uiState.downloadProgress,
                        onModelSelected = { viewModel.selectModel(it) },
                        onAddLocalModel = {
                            filePickerLauncher.launch(arrayOf("*/*")) // You can restrict to ".glb", ".gltf" if possible
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(WindowInsets.statusBars.asPaddingValues())
                    )
                }

                // Top Controls
                IconButton(
                    onClick = {
                        appNavigator.navigateTo(AppDestination.GalleryScreen)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 16.dp, start = 16.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.GridView, contentDescription = "Full Gallery")
                }

                if (arStatus == ArStatus.SUPPORTED || arStatus == ArStatus.SUPPORTED_NOT_INSTALLED) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                appNavigator.navigateTo(AppDestination.ArScreen)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        icon = { Icon(Icons.Rounded.ViewInAr, null) },
                        text = { Text("View in AR") },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .padding(top = 16.dp, end = 16.dp)
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}
