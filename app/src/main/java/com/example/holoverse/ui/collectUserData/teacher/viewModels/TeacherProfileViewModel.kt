package com.example.holoverse.ui.collectUserData.teacher.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.holoverse.auth.domain.entities.Teacher
import com.example.holoverse.ui.commonPart.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TeacherProfileViewModel @Inject constructor() : BaseValidationViewModel() {

    private val _teacherScreenState = MutableStateFlow(TeacherState())
    val teacherScreenState: StateFlow<TeacherState> = _teacherScreenState.asStateFlow()


    var selectedItem by mutableStateOf("Select Your Gender")

    private var bioValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.Bio)

    private var addressValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.ADDRESS)

    private var phoneNumberValidationState =
        ValidationState(type = TextFieldType.PhoneNumber, id = SignUpTextField.PHONE_NUMBER)

    private var genderValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.Gender)

    init {
        forms[SignUpTextField.Bio] = bioValidationState
        forms[SignUpTextField.ADDRESS] = addressValidationState
        forms[SignUpTextField.PHONE_NUMBER] = phoneNumberValidationState
        forms[SignUpTextField.Gender] = genderValidationState
    }



}


data class TeacherState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    var teacher: Teacher? = null
)

enum class SignUpTextField : TextFieldId {
    PHONE_NUMBER, ADDRESS, Bio, Gender, DATE_OF_BIRTH
}