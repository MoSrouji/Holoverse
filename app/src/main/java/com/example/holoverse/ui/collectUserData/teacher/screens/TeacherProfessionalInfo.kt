package com.example.holoverse.ui.collectUserData.teacher.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.TeacherCategory
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpTextFields
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonPart.auth.widget.CheckBoxMenu
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.commonPart.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.collectUserData.teacher.viewModels.TeacherProfessionalViewModel
import com.example.holoverse.ui.spatialTheme.SpatialBackground
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
    teacherStates: MutableStateFlow<User.Teacher>
) {
    val yearsItems = listOf("0", "+1", "+4", "+8", "+10")
    val languageItems = listOf("Arabic", "English", "France", "Italy", "Spain")
    val certificateItems = listOf("Bachelors", "Masters", "PhD")
    val specializations = TeacherCategory.getAllCategoryNames()

    val context = LocalContext.current
    var isYearsExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isSpecializationsExpanded by remember { mutableStateOf(false) }
    var isSubjectExpanded by remember { mutableStateOf(false) }
    var isCertificateExpanded by remember { mutableStateOf(false) }

    val signUpState = viewModel.signUpState.value

    LaunchedEffect(teacherStates) {
        teacherStates.collect { teacher ->
            viewModel.updateState(teacher)
        }
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val userState = User.Teacher(
                        bio = teacherStates.value.bio,
                        dateOfBirth = teacherStates.value.dateOfBirth,
                        phoneNumber = teacherStates.value.phoneNumber,
                        address = teacherStates.value.address,
                        gender = teacherStates.value.gender,
                        yearsOfExperience = viewModel.forms[SignUpTextFields.YEARS_OF_EXPERIENCE]!!.text,
                        specialization = viewModel.specializations,
                        subjects = viewModel.selectSubjects.toList(),
                        certifications = viewModel.forms[SignUpTextFields.CERTIFICATION]!!.text,
                        languagesSpoken = viewModel.selectLanguage.toList(),
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
                Toast.makeText(context, signUpState.massage, Toast.LENGTH_LONG).show()
            }
            is Response.Loading -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SpatialBackground()
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
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
                            navController.popBackStack()
                        },
                    contentDescription = "back"
                )

                TextButton(
                    onClick = {
                        navToHomeScreen()
                    },
                    modifier = Modifier.padding(end = 10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        style = IbarraNovaSemiBoldPlatinum17,
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stringResource(id = R.string.please_complete_you_auth_to_continue),
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                showIcon = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            CheckBoxMenu(
                isExpanded = isLanguageExpanded,
                onToggle = { isLanguageExpanded = !isLanguageExpanded },
                selectedItems = viewModel.selectLanguage,
                onItemSelected = { item ->
                    viewModel.updateLanguage(item)
                    val currentText = viewModel.forms[SignUpTextFields.LANGUAGE_SPOKEN]?.text ?: ""
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFields.LANGUAGE_SPOKEN]!!.copy(
                                text = if (currentText.isEmpty()) item else "$currentText, $item"
                            )
                        )
                    )
                },
                state = viewModel.forms[SignUpTextFields.LANGUAGE_SPOKEN]!!,
                menuItems = languageItems,
                ifItEmptyText = "Select Your Language",
                labelText = "Language :"
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                showIcon = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            RadioButtonMenu(
                isExpanded = isSpecializationsExpanded,
                onToggle = { isSpecializationsExpanded = !isSpecializationsExpanded },
                selectedItem = viewModel.selectSpecializations,
                onItemSelected = { item ->
                    viewModel.selectSpecializations = item
                    viewModel.selectSubjects = emptySet<String>()
                    isSpecializationsExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFields.SPECIALIZATION]!!.copy(text = item)
                        )
                    )
                    viewModel.specializations = TeacherCategory.fromString(item)
                    viewModel.subject = viewModel.specializations.specializations
                },
                state = viewModel.forms[SignUpTextFields.SPECIALIZATION]!!,
                menuItems = specializations,
                showIcon = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            CheckBoxMenu(
                isExpanded = isSubjectExpanded,
                onToggle = { isSubjectExpanded = !isSubjectExpanded },
                selectedItems = viewModel.selectSubjects,
                onItemSelected = { item ->
                    viewModel.updateSubject(item)
                    val currentText = viewModel.forms[SignUpTextFields.SUBJECTS]?.text ?: ""
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFields.SUBJECTS]!!.copy(
                                text = if (currentText.isEmpty()) item else "$currentText, $item"
                            )
                        )
                    )
                },
                state = viewModel.forms[SignUpTextFields.SUBJECTS]!!,
                menuItems = viewModel.subject,
                ifItEmptyText = "Select Your Subjects ",
                labelText = "Subjects : "
            )

            Spacer(modifier = Modifier.height(20.dp))
            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.sign_up,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (signUpState is Response.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
