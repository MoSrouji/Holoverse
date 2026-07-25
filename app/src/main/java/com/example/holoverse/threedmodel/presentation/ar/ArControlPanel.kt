package com.example.holoverse.ui.three_D_Part.ar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ArControlPanel(
    rotation: Float,
    verticalRotation: Float,
    scale: Float,
    onRotationChange: (Float) -> Unit,
    onVerticalRotationChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(4.dp)
            .width(64.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { isExpanded = !isExpanded },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.Settings,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    // Horizontal Rotation Control
                    VerticalSliderControl(
                        value = rotation,
                        onValueChange = onRotationChange,
                        valueRange = 0f..360f,
                        icon = Icons.Rounded.Sync, // Using Sync for horizontal rotation
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Vertical Rotation Control
                    VerticalSliderControl(
                        value = verticalRotation,
                        onValueChange = onVerticalRotationChange,
                        valueRange = -90f..90f,
                        icon = Icons.Rounded.ScreenRotation,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // Scale Control
                    VerticalSliderControl(
                        value = scale,
                        onValueChange = onScaleChange,
                        valueRange = 0.1f..2.0f,
                        icon = Icons.Rounded.ZoomOutMap,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalSliderControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(180.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = -90f
                    }
                    .width(140.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = color,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}
