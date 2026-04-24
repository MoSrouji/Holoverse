package com.example.holoverse.ui.three_D_Part.ar

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext



@Composable
fun rememberArStatus(): ArStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(ArStatus.UNKNOWN) }

    LaunchedEffect(Unit) {
        status = withContext(Dispatchers.IO) {
            try {
                var availability = ArCoreApk.getInstance().checkAvailability(context)
                
                // If it's still checking, wait a bit
                var retryCount = 0
                while (availability.isUnknown && retryCount < 5) {
                    delay(100)
                    availability = ArCoreApk.getInstance().checkAvailability(context)
                    retryCount++
                }

                when {
                    availability.isSupported -> {
                        if (availability == ArCoreApk.Availability.SUPPORTED_INSTALLED) {
                            ArStatus.SUPPORTED
                        } else {
                            ArStatus.SUPPORTED_NOT_INSTALLED
                        }
                    }
                    else -> ArStatus.UNSUPPORTED
                }
            } catch (e: Exception) {
                ArStatus.UNSUPPORTED
            }
        }
    }

    return status
}
