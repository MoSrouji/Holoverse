package com.example.holoverse.auth.presentation.profile_setup.student

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
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.auth.presentation.profile_setup.student.StudentPreferenceTextField
import com.example.holoverse.auth.presentation.profile_setup.student.StudentPreferenceViewModel
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.widget.CheckBoxMenu
import com.example.holoverse.auth.presentation.common.widget.RadioButtonMenu
import com.example.holoverse.auth.presentation.common.widget.button.AuthenticationButton
import com.example.holoverse.auth.presentation.common.widget.textfield.AuthenticationTextField
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
fun StudentPreferenceInfoInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: StudentPreferenceViewModel = hiltViewModel(),
    registrationViewModel: com.example.holoverse.auth.presentation.signup.RegistrationViewModel,
    darkTheme: Boolean
) {
    val gradeItems = listOf(
        "Grade 10",
        "Grade 11",
        "Grade 12",
        "University 1st Year",
        "University 2nd Year",
        "Other"
    )
    val learningTimeItems = listOf("Morning", "Afternoon", "Evening", "Night")
    val interestItems = AppCategory.entries.map { stringResource(it.titleRes) }

    val context = LocalContext.current
    var isGradeExpanded by remember { mutableStateOf(false) }
    var isLearningTimeExpanded by remember { mutableStateOf(false) }
    var isInterestsExpanded by remember { mutableStateOf(false) }

    val signUpState by viewModel.signUpState

    LaunchedEffect(Unit) {
        viewModel.updateState(registrationViewModel.studentState.value)
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val userState = registrationViewModel.studentState.value.copy(
                        currentGradeLevel = viewModel.forms[StudentPreferenceTextField.GRADE_LEVEL]!!.text,
                        universityName = viewModel.forms[StudentPreferenceTextField.UNIVERSITY_NAME]!!.text,
                        faculty = viewModel.forms[StudentPreferenceTextField.FACULTY]!!.text,
                        preferredLearningTime = viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!.text,
                        academicInterests = viewModel.selectInterests.toList()
                    )
                    viewModel.firebaseSignUp(
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
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
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
                    text = "Academic & Learning Preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            RadioButtonMenu(
                isExpanded = isGradeExpanded,
                onToggle = { isGradeExpanded = !isGradeExpanded },
                selectedItem = viewModel.selectedGradeLevel,
                onItemSelected = { item ->
                    viewModel.selectedGradeLevel = item
                    isGradeExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentPreferenceTextField.GRADE_LEVEL]!!.copy(text = item)
                        )
                    )
                },
                state = viewModel.forms[StudentPreferenceTextField.GRADE_LEVEL]!!,
                menuItems = gradeItems,
                showIcon = false,
                labelText = "Current Grade Level"
            )

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[StudentPreferenceTextField.UNIVERSITY_NAME]!!,
                hint = R.string.university,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentPreferenceTextField.UNIVERSITY_NAME]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Text
            )

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.forms[StudentPreferenceTextField.FACULTY]!!,
                hint = R.string.faculty,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentPreferenceTextField.FACULTY]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Text
            )

            RadioButtonMenu(
                isExpanded = isLearningTimeExpanded,
                onToggle = { isLearningTimeExpanded = !isLearningTimeExpanded },
                selectedItem = viewModel.selectedLearningTime,
                onItemSelected = { item ->
                    viewModel.selectedLearningTime = item
                    isLearningTimeExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!.copy(
                                text = item
                            )
                        )
                    )
                },
                state = viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!,
                menuItems = learningTimeItems,
                showIcon = false,
                labelText = "Preferred Learning Time"
            )

            CheckBoxMenu(
                isExpanded = isInterestsExpanded,
                onToggle = { isInterestsExpanded = !isInterestsExpanded },
                selectedItems = viewModel.selectInterests,
                onItemSelected = { item ->
                    viewModel.updateInterests(item)
                },
                state = viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!,
                menuItems = interestItems,
                ifItEmptyText = "Select Your Interests",
                labelText = "Academic Interests"
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
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


