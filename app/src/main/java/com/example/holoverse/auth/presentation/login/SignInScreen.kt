package com.example.holoverse.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.loading.LoadingScreen
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.HoloBlack
import androidx.compose.foundation.layout.size
import com.example.holoverse.core.ui.theme.rubik_glitch_pop
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.utils.Response

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    navToHomeScreen: () -> Unit,
    navToAdminPanel: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel(),
    darkTheme: Boolean
) {

    val context = LocalContext.current
    val signInState by viewModel.signInState

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is ValidationResultEvent.Success -> {
                    viewModel.firebaseSignIn(
                        email = viewModel.forms[SignInTextFieldId.EMAIL]!!.text.trim(),
                        password = viewModel.forms[SignInTextFieldId.PASSWORD]!!.text
                    )
                }
            }
        }
    }

    LaunchedEffect(signInState) {
        val state = signInState
        when (state) {
            is Response.Success -> {
                if (state.data) {
                    Toast.makeText(context, R.string.sign_in_successfully, Toast.LENGTH_LONG).show()
                    if (viewModel.userType.value == UserType.Admin) {
                        navToAdminPanel()
                    } else {
                        navToHomeScreen()
                    }
                }
            }
            is Response.Error -> {
                Toast.makeText(context, state.toString(), Toast.LENGTH_LONG).show()
            }
            is Response.Loading -> {}
        }
    }

    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) HoloBlack else MaterialTheme.colorScheme.background
    val contentColor = if (darkTheme) Color.White else Color.Black
    val glowOpacity = if (darkTheme) 0.15f else 0.08f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Subtle background glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(HoloPurple.copy(alpha = glowOpacity), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(HoloCyan.copy(alpha = glowOpacity), Color.Transparent)
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.Holo),
                    fontFamily = rubik_glitch_pop,
                    color = HoloCyan,
                    fontSize = 72.sp,
                    lineHeight = 70.sp
                )
                Text(
                    text = stringResource(R.string.Verse),
                    fontFamily = rubik_glitch_pop,
                    color = HoloPurple,
                    fontSize = 72.sp,
                    lineHeight = 70.sp
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.login),
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.please_sign_in_to_continue),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[SignInTextFieldId.EMAIL]!!,
                hint = R.string.email,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignInTextFieldId.EMAIL]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[SignInTextFieldId.PASSWORD]!!,
                hint = R.string.password,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignInTextFieldId.PASSWORD]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Password
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO: Implement Forget Password */ },
                text = stringResource(id = R.string.forget_password),
                color = if (darkTheme) HoloCyan else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                textId = R.string.login,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.clickable { onSignUpClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.do_not_have_an_account),
                    color = contentColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(id = R.string.sign_up),
                    color = HoloPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (signInState == Response.Loading)
            LoadingScreen()
    }
}

