package com.example.holoverse.ui.auth.widget.textfield

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.ui.theme.IbarraNovaNormalGray14
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum16


@Composable
fun AuthenticationTextField(
    modifier: Modifier,
    state: ValidationState,
    @StringRes hint: Int,
    onValueChange: (String) -> Unit,
    type: TextFieldType,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default

) {
    val darkTheme = isSystemInDarkTheme()

    CustomTextField(
        modifier = modifier,
        state = state,
        hint = hint,
        onValueChange = onValueChange,
        textStyle = IbarraNovaSemiBoldPlatinum16,
        hintTextStyle = IbarraNovaNormalGray14,
        color = MaterialTheme.colorScheme.surface,
        cornerRadius = 15.dp,
        type = type,
        keyboardOptions = keyboardOptions


        )

}