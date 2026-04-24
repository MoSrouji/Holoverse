package com.example.holoverse.ui.three_D_Part.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.three_d_model.data.local.ModelCacheManager
import com.example.holoverse.ui.three_D_Part.ModelViewModel
import com.example.holoverse.ui.three_D_Part.gallery.ModelGalleryOverlay
import com.google.ar.core.ArCoreApk
import dagger.hilt.android.lifecycle.HiltViewModel


@Composable
fun ArScreen(
    appNavigator: AppNavigator,    viewModel: ModelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cacheManager = remember { ModelCacheManager(context) }
    var cachedModelPath by remember { mutableStateOf<String?>(null) }
    val arStatus = rememberArStatus()
    var userRequestedInstall by remember { mutableStateOf(false) }

    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedModel) {
        uiState.selectedModel?.let { model ->
            isDownloading = true
            cachedModelPath = cacheManager.getModelPath(model.id, model.path)
            isDownloading = false
        }
    }

    if (arStatus == ArStatus.SUPPORTED_NOT_INSTALLED && !userRequestedInstall) {
        LaunchedEffect(Unit) {
            try {
                ArCoreApk.getInstance()
                    .requestInstall(context as android.app.Activity, !userRequestedInstall)
                userRequestedInstall = true
            } catch (e: Exception) {
                appNavigator.popBackStack()
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ArViewer(
                modelPath = cachedModelPath,
                modifier = Modifier.fillMaxSize(),
                rotation = uiState.modelRotation,
                scale = uiState.modelScale,
                isLoading = isDownloading
            )

            if (cachedModelPath != null) {
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

                if (uiState.showModelGallery) {
                    ModelGalleryOverlay(
                        models = uiState.models,
                        selectedModel = uiState.selectedModel,
                        onModelSelected = { viewModel.selectModel(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(WindowInsets.statusBars.asPaddingValues())
                    )

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
            }

            // Top Controls
            ExtendedFloatingActionButton(
                onClick = { appNavigator.popBackStack() },
                icon = { Icon(Icons.Rounded.Sync, null) },
                text = { Text("3D View") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 16.dp, end = 16.dp)
            )
        }
    }
}
