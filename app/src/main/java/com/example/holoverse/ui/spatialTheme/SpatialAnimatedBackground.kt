package com.example.holoverse.ui.spatialTheme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.ui.theme.primaryLight
import com.example.holoverse.ui.theme.secondaryLight
import kotlin.math.sin

@Preview
@Composable
fun SpatialBackground(
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    
    // This simulates the Three.js particle background using standard Canvas
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        if (!isDark) {
            // Light Theme Gradient: Lighter blue to a soft indigo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE2DFFF), // primaryContainerLight
                        Color(0xFF9CF1F0)  // secondaryContainerLight
                    )
                )
            )
        } else {
            // Dark Theme Gradient: Deep space colors
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050510), 
                        Color(0xFF1A1A2E)
                    )
                )
            )
        }


        // Draw moving particles
        // Using a deterministic pseudo-random pattern based on index
        for (i in 0..50) {
            val xOffset = (i * 137.5f) % width // Golden angle approximation
            val speed = (i % 5 + 1) * 0.5f
            val yPos = (height - ((time * speed * 10 + i * 100) % height))

            val alpha = (sin((time / 50f) + i) + 1) / 2 * 0.5f + 0.2f

            drawCircle(
                color = if (i % 2 == 0) colorScheme.primary else colorScheme.secondary,
                radius = (i % 3 + 1).dp.toPx(),
                center = Offset(xOffset, yPos),
                alpha = if (isDark) alpha else alpha * 0.6f // Subtle particles in light mode
            )
        }
    }
}
