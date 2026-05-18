package com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signin

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
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonPart.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.commonPart.auth.widget.loading.LoadingScreen
import com.example.holoverse.ui.commonPart.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.ui.theme.rubik_glitch_pop
import com.example.holoverse.utils.Response

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    navToHomeScreen: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel(),
    darkTheme: Boolean
) {

    val context = LocalContext.current
    val signInState = viewModel.signInState.value

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
        when (signInState) {
            is Response.Success -> {
                if (signInState.data) {
                    Toast.makeText(context, R.string.sign_in_successfully, Toast.LENGTH_LONG).show()
                    navToHomeScreen()
                }
            }
            is Response.Error -> {
                Toast.makeText(context, signInState.massage, Toast.LENGTH_LONG).show()
            }
            is Response.Loading -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Transparent)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.padding(bottom = 60.dp))
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.Holo),
                    fontFamily = rubik_glitch_pop,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 70.sp
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.Verse),
                    fontFamily = rubik_glitch_pop,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 70.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.login),
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.please_sign_in_to_continue),
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(15.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignInTextFieldId.EMAIL]!!, hint = R.string.email,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignInTextFieldId.EMAIL]!!.copy(text = it)
                        )
                    )
                }, type = TextFieldType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignInTextFieldId.PASSWORD]!!, hint = R.string.password,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignInTextFieldId.PASSWORD]!!.copy(text = it)
                        )
                    )
                }, type = TextFieldType.Password
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.login,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
            Spacer(modifier = Modifier.height(15.dp))

            Text(
                modifier = Modifier.clickable {
                    navToHomeScreen()
                },
                text = stringResource(id = R.string.forget_password),
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(60.dp))

            Row(modifier = Modifier.clickable {
                onSignUpClick()
            }) {
                Text(
                    text = stringResource(id = R.string.do_not_have_an_account),
                    color = colorResource(R.color.white)
                )

                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.sign_up),
                    color = colorResource(R.color.white)
                )
            }
            Spacer(
                modifier = Modifier
                    .width(10.dp)
                    .padding(bottom = 10.dp)
            )
        }

        if (signInState == Response.Loading)
            LoadingScreen()
    }
}
