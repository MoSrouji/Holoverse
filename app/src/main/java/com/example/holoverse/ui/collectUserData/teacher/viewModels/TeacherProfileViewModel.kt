package com.example.holoverse.ui.collectUserData.teacher.viewModels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.holoverse.auth.domain.entities.User
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

    var selectedImageUri by mutableStateOf<Uri?>(null)

    var selectedItem by mutableStateOf("Select Your Gender")

    private var bioValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.Bio)

    private var addressValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.ADDRESS)

    private var phoneNumberValidationState =
        ValidationState(type = TextFieldType.PhoneNumber, id = SignUpTextField.PHONE_NUMBER)

    private var genderValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.Gender)

    private var dateOfBirthValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextField.DATE_OF_BIRTH)

    init {
        forms[SignUpTextField.Bio] = bioValidationState
        forms[SignUpTextField.ADDRESS] = addressValidationState
        forms[SignUpTextField.PHONE_NUMBER] = phoneNumberValidationState
        forms[SignUpTextField.Gender] = genderValidationState
        forms[SignUpTextField.DATE_OF_BIRTH] = dateOfBirthValidationState
    }



}


data class TeacherState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    var mentor: User.Mentor? = null
)

enum class SignUpTextField : TextFieldId {
    PHONE_NUMBER, ADDRESS, Bio, Gender, DATE_OF_BIRTH
}