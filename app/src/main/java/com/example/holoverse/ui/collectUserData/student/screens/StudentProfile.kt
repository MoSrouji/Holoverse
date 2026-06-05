package com.example.holoverse.ui.collectUserData.student.screens

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
import com.example.holoverse.ui.collectUserData.student.viewModels.StudentProfileViewModel
import com.example.holoverse.ui.collectUserData.student.viewModels.StudentSignUpTextField
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonPart.auth.widget.DatePickerInput
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.commonPart.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.commonPart.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum17
import com.example.holoverse.utils.AnimatedAlertDialog
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun StudentProfileInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: StudentProfileViewModel = hiltViewModel(),
    studentStates: MutableStateFlow<User.Student>,
    darkTheme: Boolean
) {
    val genderItems = listOf("Male", "Female")
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.selectedImageUri = uri
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    studentStates.value = studentStates.value.copy(
                        phoneNumber = viewModel.forms[StudentSignUpTextField.PHONE_NUMBER]?.text
                            ?: "",
                        address = viewModel.forms[StudentSignUpTextField.ADDRESS]?.text ?: "",
                        gender = viewModel.forms[StudentSignUpTextField.Gender]?.text ?: "",
                        dateOfBirth = viewModel.forms[StudentSignUpTextField.DATE_OF_BIRTH]?.text
                            ?: "",
                        profileImageUrl = viewModel.selectedImageUri?.toString()
                    )
                    navController.navigateTo(AppDestination.SignUpStudentPreference)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showAlert) {
            AnimatedAlertDialog(
                title = stringResource(R.string.Warning),
                text = stringResource(R.string.skip_pressed_message),
                onConfirmClick = {},
                onDismissClick = { showAlert = false }
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    modifier = Modifier
                        .width(40.dp)
                        .height(30.dp)
                        .clickable {
                            navController.popBackStack()
                        },
                    contentDescription = "back"
                )

                TextButton(
                    onClick = {
                        showAlert = true
                    },
                    modifier = Modifier.padding(end = 10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        style = IbarraNovaSemiBoldPlatinum17,
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )

            Text(
                text = stringResource(id = R.string.please_complete_you_auth_to_continue),
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white)
            )

            // Profile Photo Upload Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .border(2.dp, colorResource(R.color.white), CircleShape)
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
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Add Photo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
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
                modifier = Modifier.fillMaxWidth(0.85f),
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
                label = "Date of Birth (Required)",
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
                labelText = "Gender :"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.next,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
        }
    }
}
