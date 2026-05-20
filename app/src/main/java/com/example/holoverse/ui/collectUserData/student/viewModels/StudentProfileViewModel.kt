package com.example.holoverse.ui.collectUserData.student.viewModels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.commonPart.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor() : BaseValidationViewModel() {

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
        ValidationState(type = TextFieldType.Text, id = StudentSignUpTextField.DATE_OF_BIRTH)

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
