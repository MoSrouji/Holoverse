package com.example.holoverse.ui.collectUserData.teacher

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.holoverse.auth.domain.entities.TeacherCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.holoverse.auth.domain.use_cases.AuthUseCases
import com.example.holoverse.ui.auth.presentaiton.authentication.signup.SignUpTextFieldId
import com.example.holoverse.ui.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import kotlin.collections.set

@HiltViewModel
class TeacherProfileViewModel @Inject constructor(
    private val authenticatingUseCases: AuthUseCases
) : BaseValidationViewModel() {

    var selectedItem by mutableStateOf("Select You Gender")
    var selectedYears by mutableStateOf("Select Experience years")

    var selectedCertificate by mutableStateOf("Select Your Certificate ")

    var selectLanguage by mutableStateOf(emptySet<String>())
    var selectSpecializations by mutableStateOf(" Select your specializations ")
    var selectSubjects by mutableStateOf(emptySet<String>())
    var specializations by mutableStateOf(TeacherCategory.OTHER)

    var subject by mutableStateOf(listOf(""))

    fun updateLanguage(language: String) {
        if (selectLanguage.contains(" Select your Language ")) {
            selectLanguage = emptySet()
        }
        selectLanguage = if (selectLanguage.contains(language)) {
            selectLanguage - language
        } else {

            selectLanguage + language

        }

    }

    fun updateSubject(subject: String) {
        if (selectSubjects.contains(" Select your Subjects")) {
            selectSubjects = emptySet()
        }
        selectSubjects = if (selectSubjects.contains(subject)) {
            selectSubjects - subject
        } else {

            selectSubjects + subject

        }

    }


    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState


    private var bioValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.Bio)

    private var addressValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.ADDRESS)

    private var phoneNumberValidationState =
        ValidationState(type = TextFieldType.PhoneNumber, id = SignUpTextFieldId.PHONE_NUMBER)
    private var genderValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.Gender)

    // Professional Information
    private var yearsOfExperienceValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.YEARS_OF_EXPERIENCE)
    private var specializationValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.SPECIALIZATION)
    private var subjectsValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.SUBJECTS)
    private var certificationsValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.CERTIFICATION)
    private var languagesSpokenValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.LANGUAGE_SPOKEN)
    private var hourlyRateValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.HOURLY_RATE)
    private var dateOfBirthValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.DATE_OF_BIRTH)


    init {
        forms[SignUpTextFieldId.Bio] = bioValidationState
        forms[SignUpTextFieldId.ADDRESS] = addressValidationState
        forms[SignUpTextFieldId.PHONE_NUMBER] = phoneNumberValidationState
        forms[SignUpTextFieldId.Gender] = genderValidationState
        forms[SignUpTextFieldId.YEARS_OF_EXPERIENCE] = yearsOfExperienceValidationState
        forms[SignUpTextFieldId.SPECIALIZATION] = specializationValidationState
        forms[SignUpTextFieldId.SUBJECTS] = subjectsValidationState
        forms[SignUpTextFieldId.CERTIFICATION] = certificationsValidationState
        forms[SignUpTextFieldId.LANGUAGE_SPOKEN] = languagesSpokenValidationState
        forms[SignUpTextFieldId.HOURLY_RATE] = hourlyRateValidationState
        forms[SignUpTextFieldId.DATE_OF_BIRTH] = dateOfBirthValidationState
    }


}


