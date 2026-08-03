package com.example.holoverse.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.holoverse.R
import com.example.holoverse.core.ui.spatial.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onMediaClick: (String) -> Unit,
    onPollClick: () -> Unit,
    darkTheme: Boolean = true
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Brush(darkTheme))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Drag handle replacement
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Text(
                    text = "Share Content",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentItem(
                        icon = Icons.Default.Image,
                        label = stringResource(R.string.photo),
                        color = Color(0xFF4CAF50),
                        onClick = { onMediaClick("image"); onDismiss() }
                    )
                    AttachmentItem(
                        icon = Icons.Default.Movie,
                        label = stringResource(R.string.video),
                        color = Color(0xFFE91E63),
                        onClick = { onMediaClick("video"); onDismiss() }
                    )
                    AttachmentItem(
                        icon = Icons.Rounded.ViewInAr,
                        label = "3D Model",
                        color = Color(0xFF2196F3),
                        onClick = { onMediaClick("glb"); onDismiss() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentItem(
                        icon = Icons.Default.Description,
                        label = "Document",
                        color = Color(0xFFFF9800),
                        onClick = { onMediaClick("pdf"); onDismiss() }
                    )
                    AttachmentItem(
                        icon = Icons.Default.Mic,
                        label = "Audio",
                        color = Color(0xFF9C27B0),
                        onClick = { onMediaClick("audio"); onDismiss() }
                    )
                    AttachmentItem(
                        icon = Icons.Default.Poll,
                        label = "Poll",
                        color = Color(0xFF00BCD4),
                        onClick = { onPollClick(); onDismiss() }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AttachmentItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .width(80.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}
