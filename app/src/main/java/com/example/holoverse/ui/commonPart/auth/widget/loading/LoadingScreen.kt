package com.example.holoverse.ui.commonpart.auth.widget.loading


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.holoverse.ui.theme.ColorGunmetal50
import com.example.holoverse.ui.theme.ColorVerdigris
import com.example.holoverse.ui.theme.HoloverseTheme


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen() {


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .background(ColorGunmetal50)
    ) {
        LoadingIndicator(
            color = ColorVerdigris
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    HoloverseTheme {
        LoadingScreen()
    }
}
