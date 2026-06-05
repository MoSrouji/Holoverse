package com.example.holoverse.ui.collectUserData.teacher.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.ui.collectUserData.teacher.viewModels.TeacherProfessionalViewModel
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpTextFields
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonPart.auth.widget.CheckBoxMenu
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.commonPart.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.commonPart.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum17
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TeacherProfessionalInfoInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: TeacherProfessionalViewModel = hiltViewModel(),
    mentorStates: MutableStateFlow<User.Mentor>,
    darkTheme: Boolean
) {
    val yearsItems = listOf("0", "+1", "+4", "+8", "+10")
    val languageItems = listOf("Arabic", "English", "France", "Italy", "Spain")
    val certificateItems = listOf("Bachelors", "Masters", "PhD")
    val categoryMap = AppCategory.entries.associateWith { stringResource(it.titleRes) }
    val specializationNames = categoryMap.values.toList()

    val context = LocalContext.current
    var isYearsExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isSpecializationsExpanded by remember { mutableStateOf(false) }
    var isSubjectExpanded by remember { mutableStateOf(false) }
    var isCertificateExpanded by remember { mutableStateOf(false) }

    val signUpState = viewModel.signUpState.value

    LaunchedEffect(mentorStates) {
        mentorStates.collect { teacher ->
            viewModel.updateState(teacher)
        }
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val userState = User.Mentor(
                        bio = mentorStates.value.bio,
                        dateOfBirth = mentorStates.value.dateOfBirth,
                        phoneNumber = mentorStates.value.phoneNumber,
                        address = mentorStates.value.address,
                        gender = mentorStates.value.gender,
                        profileImageUrl = mentorStates.value.profileImageUrl,
                        yearsOfExperience = viewModel.forms[SignUpTextFields.YEARS_OF_EXPERIENCE]!!.text,
                        specialization = viewModel.specializations,
                        subjects = listOf(viewModel.selectSubjects),
                        certifications = viewModel.forms[SignUpTextFields.CERTIFICATION]!!.text,
                        languagesSpoken = viewModel.selectLanguage.toList(),
                        hourlyRate = viewModel.forms[SignUpTextFields.HOURLY_RATE]!!.text.toDoubleOrNull()
                    )
                    viewModel.firebaseSingUp(userDto = userState)
                }
            }
        }
    }

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is Response.Success -> {
                if (signUpState.data) {
                    Toast.makeText(context, R.string.fill_the_form, Toast.LENGTH_LONG).show()
                    navToHomeScreen()
                }
            }

            is Response.Error -> {
                Toast.makeText(context, signUpState.toString(), Toast.LENGTH_LONG).show()
            }

            is Response.Loading -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 30.dp)
        ) {
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
                        navToHomeScreen()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        style = IbarraNovaSemiBoldPlatinum17,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us about your professional expertise",
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white).copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // Professional Details Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RadioButtonMenu(
                    isExpanded = isYearsExpanded,
                    onToggle = { isYearsExpanded = !isYearsExpanded },
                    selectedItem = viewModel.selectedYears,
                    onItemSelected = { item ->
                        viewModel.selectedYears = item
                        isYearsExpanded = false
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.YEARS_OF_EXPERIENCE]!!.copy(text = item)
                            )
                        )
                    },
                    state = viewModel.forms[SignUpTextFields.YEARS_OF_EXPERIENCE]!!,
                    menuItems = yearsItems,
                    showIcon = false,
                    labelText = "Years of Experience"
                )

                CheckBoxMenu(
                    isExpanded = isLanguageExpanded,
                    onToggle = { isLanguageExpanded = !isLanguageExpanded },
                    selectedItems = viewModel.selectLanguage,
                    onItemSelected = { item ->
                        viewModel.updateLanguage(item)
                        val currentText = viewModel.selectLanguage.joinToString(", ")
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.LANGUAGE_SPOKEN]!!.copy(
                                    text = currentText
                                )
                            )
                        )
                    },
                    state = viewModel.forms[SignUpTextFields.LANGUAGE_SPOKEN]!!,
                    menuItems = languageItems,
                    ifItEmptyText = "Select Your Languages",
                    labelText = "Languages Spoken"
                )

                RadioButtonMenu(
                    isExpanded = isCertificateExpanded,
                    onToggle = { isCertificateExpanded = !isCertificateExpanded },
                    selectedItem = viewModel.selectedCertificate,
                    onItemSelected = { item ->
                        viewModel.selectedCertificate = item
                        isCertificateExpanded = false
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.CERTIFICATION]!!.copy(text = item)
                            )
                        )
                    },
                    state = viewModel.forms[SignUpTextFields.CERTIFICATION]!!,
                    menuItems = certificateItems,
                    showIcon = false,
                    labelText = "Highest Qualification"
                )

                RadioButtonMenu(
                    isExpanded = isSpecializationsExpanded,
                    onToggle = { isSpecializationsExpanded = !isSpecializationsExpanded },
                    selectedItem = viewModel.selectSpecializations,
                    onItemSelected = { item ->
                        val selectedCategory = categoryMap.entries.find { it.value == item }?.key ?: AppCategory.OTHER
                        viewModel.selectSpecializations = item
                        viewModel.selectSubjects = "Select your Subjects"
                        isSpecializationsExpanded = false
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.SPECIALIZATION]!!.copy(text = item)
                            )
                        )
                        viewModel.specializations = selectedCategory
                        viewModel.subject = if (selectedCategory.specializations.isEmpty()) {
                            listOf(item)
                        } else {
                            selectedCategory.specializations.map { context.resources.getString(it) }
                        }
                    },
                    state = viewModel.forms[SignUpTextFields.SPECIALIZATION]!!,
                    menuItems = specializationNames,
                    showIcon = false,
                    labelText = "Main Specialization"
                )

                RadioButtonMenu(
                    isExpanded = isSubjectExpanded,
                    onToggle = { isSubjectExpanded = !isSubjectExpanded },
                    selectedItem = viewModel.selectSubjects,
                    onItemSelected = { item ->
                        viewModel.updateSubject(item)
                        isSubjectExpanded = false
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.SUBJECTS]!!.copy(
                                    text = item
                                )
                            )
                        )
                    },
                    state = viewModel.forms[SignUpTextFields.SUBJECTS]!!,
                    menuItems = viewModel.subject,
                    showIcon = false,
                    labelText = "Specific Subjects"
                )

                AuthenticationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = viewModel.forms[SignUpTextFields.HOURLY_RATE]!!,
                    hint = R.string.hourlyRate,
                    onValueChange = {
                        viewModel.onEvent(
                            ValidationEvent.TextFieldValueChange(
                                viewModel.forms[SignUpTextFields.HOURLY_RATE]!!.copy(text = it)
                            )
                        )
                    },
                    type = TextFieldType.Text,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(55.dp),
                textId = R.string.sign_up,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (signUpState is Response.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
