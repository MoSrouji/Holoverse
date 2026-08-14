package com.example.holoverse.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.core.utils.ConnectivityObserver
import kotlinx.coroutines.delay

@Composable
fun ConnectivityStatus(status: ConnectivityObserver.Status) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        isVisible = when (status) {
            ConnectivityObserver.Status.Available -> {
                delay(2000)
                false
            }
            else -> true
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (status == ConnectivityObserver.Status.Available) Color(0xFF4CAF50) else Color(0xFFF44336),
        label = "ConnectivityBackgroundColor"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (status) {
                        ConnectivityObserver.Status.Available -> "Back Online"
                        ConnectivityObserver.Status.Unavailable -> "No Internet Connection"
                        ConnectivityObserver.Status.Losing -> "Connectivity issues detected"
                        ConnectivityObserver.Status.Lost -> "Connection Lost"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
