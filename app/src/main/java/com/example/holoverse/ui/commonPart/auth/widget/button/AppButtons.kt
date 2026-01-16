package com.example.holoverse.ui.commonPart.auth.widget.button

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.holoverse.ui.theme.ColorVerdigris
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldGraniteGray


@Composable
fun AuthenticationButton(
    modifier: Modifier,
    @StringRes textId: Int,
    onClick: () -> Unit,
) {

    CustomButton(
        modifier = modifier,
        color = ColorVerdigris,
        onClick = onClick,
        textId = textId,
        textStyle = IbarraNovaSemiBoldGraniteGray,
        cornerRadius = 25.dp,
    )
}


