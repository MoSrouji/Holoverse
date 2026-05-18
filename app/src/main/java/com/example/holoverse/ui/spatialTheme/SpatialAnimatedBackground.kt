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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun SpatialBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val secondaryColor = colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    val density = LocalDensity.current
    // Reduced particle count from 51 to 25 for better performance
    val particleProps = remember(density) {
        List(25) { i ->
            val speed = (i % 5 + 1) * 0.4f
            val radiusPx = with(density) { (i % 2 + 1).dp.toPx() }
            val isPrimary = i % 2 == 0
            val xOffsetFactor = i * 137.5f
            val yOffsetFactor = i * 100f
            ParticleData(speed, radiusPx, isPrimary, xOffsetFactor, yOffsetFactor)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer() // Use graphics layer to isolate rendering
    ) {
        val width = size.width
        val height = size.height

        if (isDark) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF050510), Color(0xFF1A1A2E))
                )
            )
        } else {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE2DFFF), Color(0xFF9CF1F0))
                )
            )
        }

        val basePhase = time / 50f
        particleProps.forEachIndexed { i, p ->
            val xOffset = p.xOffsetFactor % width
            val yPos = (height - ((time * p.speed * 8 + p.yOffsetFactor) % height))
            // Simplified alpha calculation
            val alpha = (sin(basePhase + i) + 1) * 0.2f + 0.1f

            drawCircle(
                color = if (p.isPrimary) primaryColor else secondaryColor,
                radius = p.radiusPx,
                center = Offset(xOffset, yPos),
                alpha = if (isDark) alpha * 0.7f else alpha
            )
        }
    }
}

private data class ParticleData(
    val speed: Float,
    val radiusPx: Float,
    val isPrimary: Boolean,
    val xOffsetFactor: Float,
    val yOffsetFactor: Float
)
