package com.example.holoverse.ui.commonpart.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
import com.example.holoverse.ui.spatialtheme.Brush
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.utils.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    darkTheme: Boolean = true,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val changePasswordState by viewModel.changePasswordState.collectAsState()

    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is Response.Success -> {
                if ((changePasswordState as Response.Success<Boolean>).data) {
                    Toast.makeText(context, R.string.password_changed_successfully, Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            }
            is Response.Error -> {
                Toast.makeText(context, (changePasswordState as Response.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.validationEvent.collect { event ->
            if (event is ValidationResultEvent.Success) {
                viewModel.changePassword()
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Change Password",
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbarraNovaFont,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                viewModel.forms[ChangePasswordTextFieldId.OLD_PASSWORD]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.current_password,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Password
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                viewModel.forms[ChangePasswordTextFieldId.NEW_PASSWORD]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.new_password,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Password
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                viewModel.forms[ChangePasswordTextFieldId.CONFIRM_PASSWORD]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.confirm_password,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Password
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                AuthenticationButton(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    textId = R.string.update,
                    onClick = {
                        viewModel.onEvent(ValidationEvent.Submit)
                    }
                )
            }

            if (changePasswordState is Response.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

