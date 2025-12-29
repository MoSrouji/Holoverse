package com.example.holoverse.ui.auth.presentaiton.authentication.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.domain.use_cases.AuthUseCases
import com.example.holoverse.ui.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import javax.inject.Inject
import kotlin.collections.set


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticatingUseCases: AuthUseCases,
) : BaseValidationViewModel() {

    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState



    private var fullNameValidationState =
        ValidationState(type = TextFieldType.Text, id = SignUpTextFieldId.FULL_NAME)


    private var emailValidationState =
        ValidationState(type = TextFieldType.Email, id = SignUpTextFieldId.EMAIL)

    private var passwordValidationState =
        ValidationState(type = TextFieldType.Password, id = SignUpTextFieldId.PASSWORD)
    private var accountValidationState =
        ValidationState(type = TextFieldType.AccountType, id = SignUpTextFieldId.ACCOUNT_TYPE)


    var selectedItem by mutableStateOf("Select User Type")


    fun getUserType(): UserType {
        val selectedType = forms[SignUpTextFieldId.ACCOUNT_TYPE]?.text?.lowercase() ?: "student"
        return when (selectedType) {
            "student" -> UserType.Student
            "teacher" -> UserType.Teacher
            else -> UserType.Student
        }
    }


    init {
        forms[SignUpTextFieldId.FULL_NAME] = fullNameValidationState
        forms[SignUpTextFieldId.EMAIL] = emailValidationState
        forms[SignUpTextFieldId.PASSWORD] = passwordValidationState
        forms[SignUpTextFieldId.ACCOUNT_TYPE] = accountValidationState
    }


    fun restUser() {
        _signUpState.value = Response.Success(false)
    }

    fun firebaseSingUp(
        userDto: User,
        password: String
    ) {

        viewModelScope.launch {
            authenticatingUseCases.firebaseSignUp(
                userDto = userDto,
                password = password
            ).collect {
                _signUpState.value = it
            }
//            sharedViewModel.teacher.value.copy(
//                fullName = userDto.fullName ,
//                email = userDto.email,
//                accountType = userDto.accountType,
//                userId = userDto.userId
//
//
//            )
           // sharedViewModel.updateTeacher()
        }

    }


}

enum class SignUpTextFieldId : TextFieldId {
    FULL_NAME, EMAIL, PASSWORD, ACCOUNT_TYPE
}

enum class SignUpTextFields: TextFieldId{
    YEARS_OF_EXPERIENCE, SPECIALIZATION, SUBJECTS, CERTIFICATION, LANGUAGE_SPOKEN, HOURLY_RATE
}