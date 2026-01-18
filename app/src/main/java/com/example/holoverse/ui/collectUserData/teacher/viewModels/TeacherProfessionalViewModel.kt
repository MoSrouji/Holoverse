package com.example.holoverse.ui.collectUserData.teacher.viewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.TeacherCategory
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpTextFields
import com.example.holoverse.ui.commonPart.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherProfessionalViewModel @Inject constructor(
    private val authenticatingRepo: AuthRepository,
) : BaseValidationViewModel() {

    private val _teacherScreenState = MutableStateFlow(User.Teacher())
    val teacherScreenState: StateFlow<User.Teacher> = _teacherScreenState.asStateFlow()


    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState

    // Update the state from parent component
    fun updateState(state: User.Teacher) {
        _teacherScreenState.value = state.copy()
    }

    // Add a function to update professional info
    fun updateProfessionalInfo(
        years: String,
        specialization: TeacherCategory,
        subjects: List<String>,
        certifications: String,
        languages: List<String>
    ) {
        _teacherScreenState.value = _teacherScreenState.value.copy(
            yearsOfExperience = years,
            specialization = specialization,
            subjects = subjects,
            certifications = certifications,
            languagesSpoken = languages
        )
    }

    var selectedYears by mutableStateOf("Select Experience years")
    var selectedCertificate by mutableStateOf("Select Your Certificate ")
    var selectLanguage by mutableStateOf(emptySet<String>())
    var selectSpecializations by mutableStateOf("Select your specializations ")
    var selectSubjects by mutableStateOf(emptySet<String>())
    var specializations by mutableStateOf(TeacherCategory.OTHER)

    var subject by mutableStateOf(listOf(""))

    fun updateLanguage(language: String) {
        if (selectLanguage.contains("Select your Language ")) {
            selectLanguage = emptySet()
        }
        selectLanguage = if (selectLanguage.contains(language)) {
            selectLanguage - language
        } else {
            selectLanguage + language
        }
    }

    fun updateSubject(subject: String) {
        if (selectSubjects.contains("Select your Subjects")) {
            selectSubjects = emptySet()
        }
        selectSubjects = if (selectSubjects.contains(subject)) {
            selectSubjects - subject
        } else {
            selectSubjects + subject
        }
    }


    // Professional Information
    private var yearsOfExperienceValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.YEARS_OF_EXPERIENCE)
    private var specializationValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.SPECIALIZATION)
    private var subjectsValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.SUBJECTS)
    private var certificationsValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.CERTIFICATION)
    private var languagesSpokenValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.LANGUAGE_SPOKEN)
    private var hourlyRateValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFields.HOURLY_RATE)

    init {
        forms[SignUpTextFields.YEARS_OF_EXPERIENCE] = yearsOfExperienceValidationState
        forms[SignUpTextFields.SPECIALIZATION] = specializationValidationState
        forms[SignUpTextFields.SUBJECTS] = subjectsValidationState
        forms[SignUpTextFields.CERTIFICATION] = certificationsValidationState
        forms[SignUpTextFields.LANGUAGE_SPOKEN] = languagesSpokenValidationState
        //forms[SignUpTextFields.HOURLY_RATE] = hourlyRateValidationState
    }

    fun firebaseSingUp(
        userDto: User.Teacher,
    ) {

        viewModelScope.launch {
            authenticatingRepo.updateTeacherProfile(
                teacher = userDto

            ).collect {
                _signUpState.value = it
            }
        }
    }
}
