package com.example.holoverse.ui.spatialTheme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


fun Brush(darkTheme: Boolean ): Brush{
    return Brush.verticalGradient(
        colors = if (darkTheme)
            listOf(Color(0xFF050510), Color(0xFF1A1A2E))
        else
            listOf(Color(0xFFE2DFFF), Color(0xFF9CF1F0))
    )

}