package com.example.holoverse.ui.collectUserData.teacher.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.Teacher
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.navigation.AppScreen
import com.example.holoverse.ui.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.ui.auth.widget.DatePickerInput
import com.example.holoverse.ui.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.collectUserData.teacher.viewModels.SignUpTextField
import com.example.holoverse.ui.collectUserData.teacher.viewModels.TeacherProfileViewModel
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum17
import com.example.holoverse.utils.AnimatedAlertDialog
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TeacherProfileInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: TeacherProfileViewModel = hiltViewModel(),
    teacherStates: MutableStateFlow<Teacher>


) {


    val genderItems = listOf("Male", "Female")
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // 2. Dummy state for validation (Replace with your actual validation logic)
    val dateValidationState = remember {
        mutableStateOf(ValidationState(id = SignUpTextField.DATE_OF_BIRTH))
    }

    var showAlert by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    var isMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    teacherStates.value = teacherStates.value.copy(
                        bio = viewModel.forms[SignUpTextField.Bio]?.text ?: "",
                        phoneNumber = viewModel.forms[SignUpTextField.PHONE_NUMBER]?.text ?: "",
                        address = viewModel.forms[SignUpTextField.ADDRESS]?.text ?: "",
                        gender = viewModel.forms[SignUpTextField.Gender]?.text ?: "",
                        dateOfBirth = viewModel.forms[SignUpTextField.DATE_OF_BIRTH]?.text ?: ""

                    )


                    // Navigate to next screen
                    navController.navigateTo(AppScreen.SignUpTeacherProfessional)
                }


            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        if (showAlert) {
            AnimatedAlertDialog(
                title = stringResource(R.string.Warning),
                text = stringResource(R.string.skipPresed),
                onConfirmClick = {},
                onDismissClick = { showAlert = false }
            )
        }
        SpatialBackground()
        Column(

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()


        ) {

            Spacer(modifier = Modifier.height(30.dp))



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
                          //  navController.popBackStack()
                        },
                    contentDescription = "back"
                )

                TextButton(
                    onClick = {
                        showAlert = true
                        navToHomeScreen

                    },
                    modifier = Modifier
                        .padding(end = 10.dp),


                    ) {
                    Text(
                        text = stringResource(R.string.skip),
                        style = IbarraNovaSemiBoldPlatinum17,
                    )
                }

            }

            Spacer(modifier = Modifier.height(30.dp))

            //Create Account Text
            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(5.dp))

            //Please Fill Text
            Text(
                text = stringResource(id = R.string.please_complete_you_auth_to_continue),
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white)


            )
            Spacer(modifier = Modifier.height(40.dp))



            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextField.Bio]!!,
                hint = R.string.bio,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextField.Bio]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))



            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextField.PHONE_NUMBER]!!,
                hint = R.string.phoneNumber,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextField.PHONE_NUMBER]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.PhoneNumber,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(20.dp))


            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextField.ADDRESS]!!,
                hint = R.string.address,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextField.ADDRESS]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))



            DatePickerInput(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { newDateMillis ->
                    // Update the state when a date is selected from the dialog
                    selectedDateMillis = newDateMillis

                    // Example of how you might update validation state (if needed)
                    if (newDateMillis != null) {
                        dateValidationState.value = dateValidationState.value.copy(
                            hasError = false,
                            errorMessageId = null
                        )
                    }
                },
                state = dateValidationState.value,
                label = "Date of Birth (Required)",
                showIcon = true
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
                            viewModel.forms[SignUpTextField.Gender]!!.copy(text = item)
                        )
                    )

                },
                state = viewModel.forms[SignUpTextField.Gender]!!,
                menuItems = genderItems
            )



            Spacer(modifier = Modifier.height(40.dp))

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

