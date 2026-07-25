package com.example.holoverse.auth.presentation.common.widget.textfield

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.holoverse.core.ui.theme.GlassWhite
import com.example.holoverse.auth.presentation.common.util.TextFieldType

import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.core.ui.theme.IbarraNovaNormalGray14
import com.example.holoverse.core.ui.theme.IbarraNovaSemiBoldPlatinum16


@Composable
fun AuthenticationTextField(
    modifier: Modifier,
    state: ValidationState,
    @StringRes hint: Int,
    onValueChange: (String) -> Unit,
    type: TextFieldType,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    CustomTextField(
        modifier = modifier,
        state = state,
        hint = hint,
        onValueChange = onValueChange,
        textStyle = IbarraNovaSemiBoldPlatinum16,
        hintTextStyle = IbarraNovaNormalGray14,
        cornerRadius = 15.dp,
        type = type,
        keyboardOptions = keyboardOptions
    )
}

