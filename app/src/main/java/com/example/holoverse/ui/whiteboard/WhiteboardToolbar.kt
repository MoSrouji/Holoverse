package com.example.holoverse.ui.whiteboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun WhiteboardToolbar(
    manager: WhiteboardManager,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(Icons.Default.Edit, "Pen", manager.currentTool == WhiteboardTool.PEN) {
                    manager.currentTool = WhiteboardTool.PEN
                }
                ToolButton(
                    Icons.Default.AutoFixNormal,
                    "Eraser",
                    manager.currentTool == WhiteboardTool.ERASER
                ) {
                    manager.currentTool = WhiteboardTool.ERASER
                }
                ToolButton(
                    Icons.Default.Rectangle,
                    "Rect",
                    manager.currentTool == WhiteboardTool.RECTANGLE
                ) {
                    manager.currentTool = WhiteboardTool.RECTANGLE
                }
                ToolButton(
                    Icons.Default.Circle,
                    "Circle",
                    manager.currentTool == WhiteboardTool.CIRCLE
                ) {
                    manager.currentTool = WhiteboardTool.CIRCLE
                }
                ToolButton(
                    Icons.Default.HorizontalRule,
                    "Line",
                    manager.currentTool == WhiteboardTool.LINE
                ) {
                    manager.currentTool = WhiteboardTool.LINE
                }
                ToolButton(
                    Icons.Default.TrendingFlat,
                    "Arrow",
                    manager.currentTool == WhiteboardTool.ARROW
                ) {
                    manager.currentTool = WhiteboardTool.ARROW
                }
                VerticalDivider(modifier = Modifier.height(32.dp))
                IconButton(onClick = { manager.undo() }) {
                    Icon(Icons.Default.Undo, "Undo")
                }
                IconButton(onClick = { manager.clear() }) {
                    Icon(Icons.Default.Delete, "Clear")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Color.Black,
                    Color.Red,
                    Color.Green,
                    Color.Blue,
                    Color.Yellow
                ).forEach { color ->
                    ColorButton(color, manager.currentStyle.color == color) {
                        manager.currentStyle = manager.currentStyle.copy(color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(icon, contentDescription = label)
    }
}

@Composable
fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f)) else Modifier
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.White else color)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
    )
}
