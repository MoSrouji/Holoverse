package com.example.holoverse.ui.auth.widget.textfield


import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.R
import com.example.holoverse.ui.theme.ColorPlatinum
import com.example.holoverse.ui.theme.IbarraNovaNormalError13


@Composable
fun CustomTextField(
    modifier: Modifier,
    state: ValidationState,
    textStyle: TextStyle,
    @StringRes hint: Int,
    hintTextStyle: TextStyle,
    onValueChange: (String) -> Unit,
    color: Color,
    cornerRadius: Dp = 0.dp,
    type: TextFieldType,
    keyboardOptions: KeyboardOptions

) {


    var passwordVisible by remember {
        mutableStateOf(true)
    }

    var trailingId = when (type) {

        TextFieldType.Password -> R.drawable.ic_visibility_on
        else -> null
    }


    if (trailingId != null) {

        trailingId =
            if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on
    }


    Column {


        TextField(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = color, shape = RoundedCornerShape(cornerRadius)
                ),
            keyboardOptions = keyboardOptions,


            value = state.text,
            textStyle = textStyle,
            onValueChange = onValueChange,
            label = {
                Text(text = stringResource(id = hint), style = hintTextStyle, softWrap = true)
            },
            colors = TextFieldDefaults.colors(
                cursorColor = ColorPlatinum,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = color,
                unfocusedContainerColor = color,
                disabledContainerColor = color,
            ),
            visualTransformation =
                if (type == TextFieldType.Password) {

                    if (passwordVisible) PasswordVisualTransformation() else VisualTransformation.None

                } else VisualTransformation.None,
            trailingIcon = {

                if (trailingId != null) {

                    Icon(
                        modifier = Modifier.clickable {
                            passwordVisible = !passwordVisible
                        },
                        painter = painterResource(id = trailingId), contentDescription = "password",
                        tint = ColorPlatinum
                    )
                }
            }

        )

        if (state.hasError && state.errorMessageId != null) {

            Text(
                text = stringResource(id = state.errorMessageId),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp),
                style = IbarraNovaNormalError13
            )
        }

    }


}

