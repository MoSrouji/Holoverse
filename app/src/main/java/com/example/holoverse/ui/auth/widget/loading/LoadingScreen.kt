package com.example.holoverse.ui.auth.widget.loading


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.holoverse.ui.theme.ColorGunmetal50
import com.example.holoverse.ui.theme.ColorVerdigris


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoadingScreen() {


    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .background(ColorGunmetal50)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp), color = ColorVerdigris)

    }
}