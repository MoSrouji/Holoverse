package com.example.holoverse.auth.presentation.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.domain.use_cases.AuthUseCases
import com.example.holoverse.auth.presentation.common.base.BaseValidationViewModel
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.interfaces.TextFieldId
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticatingUseCases: AuthUseCases,
    private val preferenceManager: PreferenceManager
) : BaseValidationViewModel() {

    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState


    private var fullNameValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.FULL_NAME)


    private var emailValidationState =
        ValidationState(type = TextFieldType.Email, id = SignUpTextFieldId.EMAIL)

    private var passwordValidationState =
        ValidationState(type = TextFieldType.Password, id = SignUpTextFieldId.PASSWORD)
    private var confirmPasswordValidationState =
        ValidationState(type = TextFieldType.Password, id = SignUpTextFieldId.CONFIRM_PASSWORD)
    private var accountValidationState =
        ValidationState(type = TextFieldType.AccountType, id = SignUpTextFieldId.ACCOUNT_TYPE)


    var selectedItem by mutableStateOf("Select User Type")


    fun getUserType(): UserType {
        val selectedType = forms[SignUpTextFieldId.ACCOUNT_TYPE]?.text?.lowercase() ?: "student"
        return when (selectedType) {
            "mentor" -> UserType.Mentor
            else -> UserType.Student
        }
    }


    init {
        forms[SignUpTextFieldId.FULL_NAME] = fullNameValidationState
        forms[SignUpTextFieldId.EMAIL] = emailValidationState
        forms[SignUpTextFieldId.PASSWORD] = passwordValidationState
        forms[SignUpTextFieldId.CONFIRM_PASSWORD] = confirmPasswordValidationState
        forms[SignUpTextFieldId.ACCOUNT_TYPE] = accountValidationState
        checkUserSession()
    }

    override fun onEvent(event: com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent) {
        super.onEvent(event)
        if (event is com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent.TextFieldValueChange) {
            if (event.state.id == SignUpTextFieldId.CONFIRM_PASSWORD || event.state.id == SignUpTextFieldId.PASSWORD) {
                validateConfirmPassword()
            }
        }
    }

    private fun validateConfirmPassword() {
        val password = forms[SignUpTextFieldId.PASSWORD]?.text ?: ""
        val confirmPassword = forms[SignUpTextFieldId.CONFIRM_PASSWORD]?.text ?: ""

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            forms[SignUpTextFieldId.CONFIRM_PASSWORD] = forms[SignUpTextFieldId.CONFIRM_PASSWORD]!!.copy(
                hasError = true,
                errorMessageId = com.example.holoverse.R.string.passwords_do_not_match
            )
        }
    }


    private fun checkUserSession() {
        val user = preferenceManager.getUser()
        if (user != null) {
            _signUpState.value = Response.Success(true)
        }
    }


    fun restUser() {
        _signUpState.value = Response.Success(false)
    }

    suspend fun firebaseSingUp(
        userDto: User,
        password: String
    ) {
        authenticatingUseCases.firebaseSignUp(
            userDto = userDto,
            password = password
        ).onEach { response ->
            _signUpState.value = response
            if (response is Response.Success && response.data) {
                // Fetch and save user to preferences after successful sign up
                val user = authenticatingUseCases.getCurrentUser()
                user?.let { preferenceManager.saveUser(it) }
            }
        }.launchIn(viewModelScope)
    }


}

enum class SignUpTextFieldId : TextFieldId {
    FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, ACCOUNT_TYPE
}

enum class SignUpTextFields : TextFieldId {
    YEARS_OF_EXPERIENCE, SPECIALIZATION, SUBJECTS, CERTIFICATION, LANGUAGE_SPOKEN, HOURLY_RATE
}


