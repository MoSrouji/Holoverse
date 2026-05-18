package com.example.holoverse.ui.commonPart.auth.widget.loading


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.holoverse.ui.theme.ColorGunmetal50
import com.example.holoverse.ui.theme.ColorVerdigris


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoadingScreen() {


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .background(ColorGunmetal50)
    ) {
        CircularProgressIndicator(
            color = ColorVerdigris
        )
    }
}