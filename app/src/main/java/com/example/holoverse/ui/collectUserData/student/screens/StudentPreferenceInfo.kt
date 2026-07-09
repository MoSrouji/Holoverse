package com.example.holoverse.ui.collectUserData.student.screens

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
import com.example.holoverse.ui.collectUserData.student.viewModels.StudentPreferenceTextField
import com.example.holoverse.ui.collectUserData.student.viewModels.StudentPreferenceViewModel
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
fun StudentPreferenceInfoInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: StudentPreferenceViewModel = hiltViewModel(),
    studentStates: MutableStateFlow<User.Student>,
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

    val signUpState = viewModel.signUpState.value

    LaunchedEffect(studentStates) {
        studentStates.collect { student ->
            viewModel.updateState(student)
        }
    }

    LaunchedEffect(key1 = context) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                ValidationResultEvent.Success -> {
                    val userState = studentStates.value.copy(
                        currentGradeLevel = viewModel.forms[StudentPreferenceTextField.GRADE_LEVEL]!!.text,
                        universityName = viewModel.forms[StudentPreferenceTextField.UNIVERSITY_NAME]!!.text,
                        faculty = viewModel.forms[StudentPreferenceTextField.FACULTY]!!.text,
                        preferredLearningTime = viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!.text,
                        academicInterests = viewModel.selectInterests.toList()
                    )
                    viewModel.firebaseSignUp(userDto = userState)
                }
            }
        }
    }

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is Response.Success -> {
                if (signUpState.data) {
                    navToHomeScreen()
                }
            }

            is Response.Error -> {
                Toast.makeText(context, signUpState.message, Toast.LENGTH_LONG).show()
            }

            is Response.Loading -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
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
                        viewModel.setProfileComplete(true)
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

            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )

            Text(
                text = "Academic & Learning Preferences",
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white)
            )

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
                labelText = "Current Grade Level :"
            )

            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
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
                modifier = Modifier.fillMaxWidth(0.85f),
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
                labelText = "Preferred Learning Time :"
            )

            CheckBoxMenu(
                isExpanded = isInterestsExpanded,
                onToggle = { isInterestsExpanded = !isInterestsExpanded },
                selectedItems = viewModel.selectInterests,
                onItemSelected = { item ->
                    viewModel.updateInterests(item)
                },
                state = viewModel.forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME]!!, // Reusing a state or create a dummy
                menuItems = interestItems,
                ifItEmptyText = "Select Your Interests",
                labelText = "Academic Interests :"
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.sign_up,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },
            )
        }
    }

    if (signUpState is Response.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
