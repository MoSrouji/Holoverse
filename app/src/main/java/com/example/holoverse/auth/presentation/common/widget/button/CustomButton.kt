package com.example.holoverse.auth.presentation.common.widget.button

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun CustomButton(
    modifier: Modifier,
    color: Color = Color.Transparent,
    cornerRadius: Dp = 25.dp,
    onClick: () -> Unit,
    @StringRes textId: Int,
    textStyle: TextStyle,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    gradient: Brush? = null,
    enabled: Boolean = true
) {
    val buttonModifier = if (gradient != null) {
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(gradient)
    } else {
        modifier
    }

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        contentPadding = PaddingValues(vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (gradient != null) Color.Transparent else color,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(cornerRadius),
        border = if (gradient == null && borderWidth > 0.dp) BorderStroke(width = borderWidth, color = borderColor) else null,
        enabled = enabled
    ) {
        Text(
            text = stringResource(id = textId),
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}
