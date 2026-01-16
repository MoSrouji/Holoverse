package com.example.holoverse.ui.spatialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // Add your splash screen content here
        // For example: Logo, app name, etc.
        // You can use Image, Text, or custom animations

        // Simulate some loading process
        LaunchedEffect(Unit) {
            delay(2000) // Show splash screen for 2 seconds
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
}