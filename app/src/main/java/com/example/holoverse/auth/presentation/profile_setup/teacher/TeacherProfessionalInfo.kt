package com.example.holoverse.auth.presentation.profile_setup.teacher

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.auth.presentation.profile_setup.teacher.TeacherProfessionalViewModel
import com.example.holoverse.auth.presentation.signup.SignUpTextFields
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.CheckBoxMenu
import com.example.holoverse.auth.presentation.common.widget.RadioButtonMenu
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
import com.example.holoverse.payment.presentation.components.MentorPaymentConfirmationDialog
import com.example.holoverse.core.ui.theme.IbarraNovaBoldPlatinum18
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.HoloBlack
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import com.example.holoverse.core.ui.theme.IbarraNovaSemiBoldPlatinum17
import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TeacherProfessionalInfoInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: TeacherProfessionalViewModel = hiltViewModel(),
    registrationViewModel: com.example.holoverse.auth.presentation.signup.RegistrationViewModel,
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
    var showPaymentDialog by remember { mutableStateOf(false) }

    val signUpState by viewModel.signUpState

    LaunchedEffect(Unit) {
        viewModel.updateState(registrationViewModel.mentorState.value)
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val currentMentor = registrationViewModel.mentorState.value
                    val userState = User.Mentor(
                        fullName = currentMentor.fullName,
                        email = currentMentor.email,
                        accountType = UserType.Mentor,
                        bio = currentMentor.bio,
                        dateOfBirth = currentMentor.dateOfBirth,
                        phoneNumber = currentMentor.phoneNumber,
                        address = currentMentor.address,
                        gender = currentMentor.gender,
                        profileImageUrl = currentMentor.profileImageUrl,
                        yearsOfExperience = viewModel.forms[SignUpTextFields.YEARS_OF_EXPERIENCE]!!.text,
                        specialization = viewModel.specializations,
                        subjects = listOf(viewModel.selectSubjects),
                        certifications = viewModel.forms[SignUpTextFields.CERTIFICATION]!!.text,
                        languagesSpoken = viewModel.selectLanguage.toList(),
                        hourlyRate = viewModel.forms[SignUpTextFields.HOURLY_RATE]!!.text.toDoubleOrNull()
                    )
                    viewModel.firebaseSingUp(
                        userDto = userState,
                        password = registrationViewModel.password.value
                    )
                }
            }
        }
    }

    LaunchedEffect(signUpState) {
        val state = signUpState
        when (state) {
            is Response.Success -> {
                if (state.data) {
                    navToHomeScreen()
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

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
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
                    text = "Tell us about your professional expertise",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Professional Details Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                        val selectedCategory =
                            categoryMap.entries.find { it.value == item }?.key ?: AppCategory.OTHER
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

            Spacer(modifier = Modifier.height(60.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                textId = R.string.sign_up,
                onClick = {
                    showPaymentDialog = true
                },
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showPaymentDialog) {
            MentorPaymentConfirmationDialog(
                onConfirm = {
                    showPaymentDialog = false
                    viewModel.onEvent(ValidationEvent.Submit)
                },
                onDismiss = { showPaymentDialog = false }
            )
        }
    }

    if (signUpState is Response.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}



