package com.example.holoverse.auth.presentation.signup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.RadioButtonMenu
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.loading.LoadingScreen
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
import com.example.holoverse.core.ui.theme.IbarraNovaBoldPlatinum18
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.HoloBlack
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import com.example.holoverse.core.utils.Response

import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onNavigateToTeacherProfile: () -> Unit,
    onNavigateToStudentProfile: () -> Unit,
    navToHomeScreen: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
    mentorStates: MutableStateFlow<User.Mentor>,
    studentStates: MutableStateFlow<User.Student>,
    passwordState: MutableStateFlow<String>,
    darkTheme: Boolean
) {
    val menuItems = listOf("Mentor", "Student")
    val signUpState = viewModel.signUpState.value
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val fullName = viewModel.forms[SignUpTextFieldId.FULL_NAME]!!.text
                    val email = viewModel.forms[SignUpTextFieldId.EMAIL]!!.text
                    val password = viewModel.forms[SignUpTextFieldId.PASSWORD]!!.text

                    passwordState.value = password

                    when (viewModel.getUserType()) {
                        UserType.Mentor -> {
                            mentorStates.value = mentorStates.value.copy(
                                fullName = fullName,
                                email = email,
                                accountType = UserType.Mentor
                            )
                            onNavigateToTeacherProfile()
                        }
                        UserType.Student -> {
                            studentStates.value = studentStates.value.copy(
                                fullName = fullName,
                                email = email,
                                accountType = UserType.Student
                            )
                            onNavigateToStudentProfile()
                        }
                        UserType.Admin -> throw IllegalStateException("Admin sign up is not allowed")
                    }
                }
            }
        }
    }

    // Removed sign up state handling here as it's deferred

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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.headlineMedium,
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
                state = viewModel.forms[SignUpTextFieldId.FULL_NAME]!!,
                hint = R.string.full_name,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.FULL_NAME]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[SignUpTextFieldId.EMAIL]!!,
                hint = R.string.email,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.EMAIL]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Email
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[SignUpTextFieldId.PASSWORD]!!,
                hint = R.string.password,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.PASSWORD]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Password
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[SignUpTextFieldId.CONFIRM_PASSWORD]!!,
                hint = R.string.confirm_password,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.CONFIRM_PASSWORD]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Password
            )
            Spacer(modifier = Modifier.height(20.dp))

            RadioButtonMenu(
                isExpanded = isMenuExpanded,
                onToggle = { isMenuExpanded = !isMenuExpanded },
                selectedItem = viewModel.selectedItem,
                onItemSelected = { item ->
                    viewModel.selectedItem = item
                    isMenuExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.ACCOUNT_TYPE]!!.copy(text = item)
                        )
                    )
                },
                state = viewModel.forms[SignUpTextFieldId.ACCOUNT_TYPE]!!,
                menuItems = menuItems
            )

            Spacer(modifier = Modifier.height(60.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                textId = R.string.next,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (signUpState == Response.Loading)
            LoadingScreen()
    }
}


