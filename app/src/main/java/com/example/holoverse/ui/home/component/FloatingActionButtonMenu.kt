package com.example.holoverse.ui.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.R

data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun FloatingActionButtonMenu(
    visible: Boolean,
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = spring(stiffness = 300f),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        // Menu Items
        items.forEachIndexed { index, item ->
            // Curved offset: 0, 1, 2, 3, 4
            // Offset logic: Sinusoidal curve for better aesthetics
            val xOffset = when (index) {
                0 -> 5.dp
                1 -> 60.dp
                2 -> 85.dp // Peak
                3 -> 60.dp
                4 -> 5.dp
                else -> 0.dp
            }

            // Staggered delay for "creativity"
            val delay = if (expanded) (items.size - 1 - index) * 60 else index * 30

            AnimatedVisibility(
                visible = expanded && visible,
                enter = fadeIn(animationSpec = tween(200, delayMillis = delay)) +
                        scaleIn(
                            initialScale = 0.5f,
                            animationSpec = tween(300, delayMillis = delay)
                        ) +
                        expandVertically(animationSpec = tween(300, delayMillis = delay)),
                exit = fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.8f, animationSpec = tween(150)) +
                        shrinkVertically(animationSpec = tween(150)),
                modifier = Modifier.offset(x = -xOffset)
            ) {
                FabMenuChildItem(item = item) {
                    expanded = false
                    item.onClick()
                }
            }
        }

        // Main FAB / Close Button
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val glassColor = if (expanded) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            }

            val borderColor = if (expanded) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            }

            Surface(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                glassColor.copy(alpha = glassColor.alpha + 0.1f),
                                glassColor
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = expanded,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(220, delayMillis = 90)
                                    ) togetherWith
                                    fadeOut(animationSpec = tween(90))
                        },
                        label = "fab_text_anim"
                    ) { isExpanded ->
                        if (isExpanded) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Close",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(rotation),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.teacher_tools),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FabMenuChildItem(
    item: FabMenuItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

