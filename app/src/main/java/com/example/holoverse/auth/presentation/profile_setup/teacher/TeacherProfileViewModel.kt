package com.example.holoverse.auth.presentation.profile_setup.teacher

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.core.utils.Response
import com.example.holoverse.auth.presentation.common.base.BaseValidationViewModel
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.interfaces.TextFieldId
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TeacherProfileViewModel @Inject constructor(
    private val authenticatingRepo: AuthRepository,
    private val preferenceManager: com.example.holoverse.core.utils.PreferenceManager
) : BaseValidationViewModel() {

    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: androidx.compose.runtime.State<Response<Boolean>> = _signUpState

    fun firebaseSignUp(userDto: User.Mentor, password: String) {
        viewModelScope.launch {
            authenticatingRepo.firebaseSignUp(userDto = userDto, password = password).collect {
                if (it is Response.Success && it.data) {
                    preferenceManager.setProfileComplete(true)
                }
                _signUpState.value = it
            }
        }
    }

    fun setProfileComplete(isComplete: Boolean) {
        preferenceManager.setProfileComplete(isComplete)
    }

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
        ValidationState(type = TextFieldType.Date, id = SignUpTextField.DATE_OF_BIRTH)

    init {
        forms[SignUpTextField.Bio] = bioValidationState
        forms[SignUpTextField.ADDRESS] = addressValidationState
        forms[SignUpTextField.PHONE_NUMBER] = phoneNumberValidationState
        forms[SignUpTextField.Gender] = genderValidationState
        forms[SignUpTextField.DATE_OF_BIRTH] = dateOfBirthValidationState
    }

    fun onDateSelected(millis: Long) {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = formatter.format(Date(millis))
        onEvent(
            ValidationEvent.TextFieldValueChange(
                forms[SignUpTextField.DATE_OF_BIRTH]!!.copy(
                    text = dateString,
                    hasError = false,
                    errorMessageId = null
                )
            )
        )
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
