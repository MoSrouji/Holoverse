package com.example.holoverse.auth.presentation.profile_setup.student

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.auth.presentation.profile_setup.student.StudentProfileViewModel
import com.example.holoverse.auth.presentation.profile_setup.student.StudentSignUpTextField
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.DatePickerInput
import com.example.holoverse.auth.presentation.common.widget.RadioButtonMenu
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
import com.example.holoverse.core.ui.theme.IbarraNovaBoldPlatinum18
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.HoloBlack
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.holoverse.core.ui.theme.IbarraNovaSemiBoldPlatinum17
import android.widget.Toast
import com.example.holoverse.core.utils.AnimatedAlertDialog
import com.example.holoverse.core.utils.Response
import com.example.holoverse.auth.presentation.common.widget.loading.LoadingScreen
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun StudentProfileInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: StudentProfileViewModel = hiltViewModel(),
    registrationViewModel: com.example.holoverse.auth.presentation.signup.RegistrationViewModel,
    darkTheme: Boolean
) {
    val genderItems = listOf("Male", "Female")
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }
    val signUpState by viewModel.signUpState

    LaunchedEffect(signUpState) {
        val state = signUpState
        if (state is Response.Success && state.data) {
            navToHomeScreen()
        } else if (state is Response.Error) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.selectedImageUri = uri
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val currentStudent = registrationViewModel.studentState.value
                    registrationViewModel.updateStudent(currentStudent.copy(
                        phoneNumber = viewModel.forms[StudentSignUpTextField.PHONE_NUMBER]?.text
                            ?: "",
                        address = viewModel.forms[StudentSignUpTextField.ADDRESS]?.text ?: "",
                        gender = viewModel.forms[StudentSignUpTextField.Gender]?.text ?: "",
                        dateOfBirth = viewModel.forms[StudentSignUpTextField.DATE_OF_BIRTH]?.text
                            ?: "",
                        profileImageUrl = viewModel.selectedImageUri?.toString()
                    ))
                    navController.navigateTo(AppDestination.SignUpStudentPreference)
                }
            }
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
        
        
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = contentColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.create_account),
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(id = R.string.please_complete_you_auth_to_continue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            // Profile Photo Upload Section
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.05f))
                    .border(2.dp, Brush.linearGradient(listOf(HoloCyan, HoloPurple)), CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.selectedImageUri != null) {
                    AsyncImage(
                        model = viewModel.selectedImageUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = contentColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ADD PHOTO",
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[StudentSignUpTextField.PHONE_NUMBER]!!,
                hint = R.string.phoneNumber,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentSignUpTextField.PHONE_NUMBER]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.PhoneNumber,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[StudentSignUpTextField.ADDRESS]!!,
                hint = R.string.address,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentSignUpTextField.ADDRESS]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Text
            )

            DatePickerInput(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { newDateMillis ->
                    selectedDateMillis = newDateMillis
                    if (newDateMillis != null) {
                        viewModel.onDateSelected(newDateMillis)
                    }
                },
                state = viewModel.forms[StudentSignUpTextField.DATE_OF_BIRTH]!!,
                label = "Date of Birth",
                showIcon = true
            )

            RadioButtonMenu(
                isExpanded = isMenuExpanded,
                onToggle = { isMenuExpanded = !isMenuExpanded },
                selectedItem = viewModel.selectedItem,
                onItemSelected = { item ->
                    viewModel.selectedItem = item
                    isMenuExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentSignUpTextField.Gender]!!.copy(text = item)
                        )
                    )
                },
                state = viewModel.forms[StudentSignUpTextField.Gender]!!,
                menuItems = genderItems,
                labelText = "Gender"
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                textId = R.string.next,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (signUpState is Response.Loading) {
            LoadingScreen()
        }
    }
}


