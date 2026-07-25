package com.example.holoverse.auth.presentation.common.widget.textfield


import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.BorderLight
import com.example.holoverse.core.ui.theme.GlassLight
import com.example.holoverse.core.ui.theme.BorderWhite
import com.example.holoverse.core.ui.theme.GlassWhite
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.holoverse.R
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.core.ui.theme.ColorPlatinum
import com.example.holoverse.core.ui.theme.IbarraNovaNormalError13


@Composable
fun CustomTextField(
    modifier: Modifier,
    state: ValidationState,
    textStyle: TextStyle,
    @StringRes hint: Int,
    hintTextStyle: TextStyle,
    onValueChange: (String) -> Unit,
    color: Color? = null,
    cornerRadius: Dp = 15.dp,
    type: TextFieldType,
    keyboardOptions: KeyboardOptions
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()

    val backgroundColor = color ?: if (darkTheme) GlassWhite else GlassLight
    val borderColor = if (darkTheme) BorderWhite else BorderLight
    val contentColor = if (darkTheme) Color.White else Color.Black

    val trailingId = if (type == TextFieldType.Password) {
        if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on
    } else null

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.text,
                onValueChange = onValueChange,
                textStyle = textStyle.copy(color = contentColor),
                label = {
                    Text(
                        text = stringResource(id = hint),
                        style = hintTextStyle.copy(color = contentColor.copy(alpha = 0.6f))
                    )
                },
                keyboardOptions = keyboardOptions,
                colors = TextFieldDefaults.colors(
                    cursorColor = if (darkTheme) ColorPlatinum else Color.Black,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedLabelColor = if (darkTheme) ColorPlatinum else Color.Black,
                    unfocusedLabelColor = (if (darkTheme) ColorPlatinum else Color.Black).copy(alpha = 0.7f)
                ),
                visualTransformation = if (type == TextFieldType.Password && !passwordVisible) {
                    PasswordVisualTransformation()
                } else VisualTransformation.None,
                trailingIcon = {
                    if (trailingId != null) {
                        Icon(
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible },
                            painter = painterResource(id = trailingId),
                            contentDescription = "password visibility",
                            tint = if (darkTheme) ColorPlatinum else Color.Black
                        )
                    }
                }
            )
        }

        if (state.hasError && state.errorMessageId != null) {
            Text(
                text = stringResource(id = state.errorMessageId),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, end = 8.dp),
                style = IbarraNovaNormalError13
            )
        }
    }
}



