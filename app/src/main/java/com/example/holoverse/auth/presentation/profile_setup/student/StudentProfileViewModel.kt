package com.example.holoverse.auth.presentation.profile_setup.student

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
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.interfaces.TextFieldId
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
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
class StudentProfileViewModel @Inject constructor(
    private val authenticatingRepo: AuthRepository,
    private val preferenceManager: com.example.holoverse.core.utils.PreferenceManager
) : BaseValidationViewModel() {

    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: androidx.compose.runtime.State<Response<Boolean>> = _signUpState

    fun firebaseSignUp(userDto: User.Student, password: String) {
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

    private val _studentScreenState = MutableStateFlow(StudentState())
    val studentScreenState: StateFlow<StudentState> = _studentScreenState.asStateFlow()

    var selectedItem by mutableStateOf("Select Your Gender")
    var selectedImageUri by mutableStateOf<Uri?>(null)

    private var addressValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentSignUpTextField.ADDRESS)

    private var phoneNumberValidationState =
        ValidationState(type = TextFieldType.PhoneNumber, id = StudentSignUpTextField.PHONE_NUMBER)

    private var genderValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentSignUpTextField.Gender)

    private var dateOfBirthValidationState =
        ValidationState(type = TextFieldType.Date, id = StudentSignUpTextField.DATE_OF_BIRTH)

    init {
        forms[StudentSignUpTextField.ADDRESS] = addressValidationState
        forms[StudentSignUpTextField.PHONE_NUMBER] = phoneNumberValidationState
        forms[StudentSignUpTextField.Gender] = genderValidationState
        forms[StudentSignUpTextField.DATE_OF_BIRTH] = dateOfBirthValidationState
    }

    fun onDateSelected(millis: Long) {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = formatter.format(Date(millis))
        onEvent(
            ValidationEvent.TextFieldValueChange(
                forms[StudentSignUpTextField.DATE_OF_BIRTH]!!.copy(
                    text = dateString,
                    hasError = false,
                    errorMessageId = null
                )
            )
        )
    }
}

data class StudentState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    var student: User.Student? = null
)

enum class StudentSignUpTextField : TextFieldId {
    PHONE_NUMBER, ADDRESS, Gender, DATE_OF_BIRTH
}

