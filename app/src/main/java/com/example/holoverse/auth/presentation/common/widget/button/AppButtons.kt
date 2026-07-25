package com.example.holoverse.auth.presentation.common.widget.button

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.holoverse.core.ui.theme.HoloGradient
import com.example.holoverse.core.ui.theme.ColorGunmetal
import com.example.holoverse.core.ui.theme.IbarraNovaSemiBoldGraniteGray


@Composable
fun AuthenticationButton(
    modifier: Modifier,
    @StringRes textId: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    CustomButton(
        modifier = modifier,
        gradient = HoloGradient,
        onClick = onClick,
        textId = textId,
        textStyle = IbarraNovaSemiBoldGraniteGray.copy(color = Color.White),
        cornerRadius = 25.dp,
        enabled = enabled
    )
}




