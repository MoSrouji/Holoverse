package com.example.holoverse.ui.three_D_Part.viewer

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

@Composable
fun SceneViewer(
    modelPath: String,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    
    var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        Log.d("SceneViewer", "Engine resumed: $engine")
    }
    
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        Log.d("SceneViewer", "Engine paused: $engine")
    }

    LaunchedEffect(modelPath) {
        isLoading = true
        modelInstance = null
        loadError = null
        try {
            modelInstance = modelLoader.loadModelInstance(modelPath)
            if (modelInstance == null) {
                loadError = "Failed to load model"
            }
        } catch (e: Exception) {
            loadError = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = Position(x = 0f, y = 0f, z = 4f),
        targetPosition = Position(x = 0f, y = 0f, z = 0f)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraManipulator = cameraManipulator
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 1.0f,
                    centerOrigin = Position(x = 0f, y = 0f, z = 0f),
                    isEditable = true
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        loadError?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Load Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
