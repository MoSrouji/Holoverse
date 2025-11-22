package com.example.holoverse.ui.auth.presentaiton.authentication.signin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.use_cases.AuthUseCases

import com.example.holoverse.ui.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticatingUseCases: AuthUseCases
)
    : BaseValidationViewModel() {
    private val _signInState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signInState = _signInState

    private var emailValidationState =
        ValidationState(id = SignInTextFieldId.EMAIL, type = TextFieldType.Email)

    private var passwordValidationState =
        ValidationState(id = SignInTextFieldId.PASSWORD, type = TextFieldType.Password)


    init {

        forms[SignInTextFieldId.EMAIL] = emailValidationState
        forms[SignInTextFieldId.PASSWORD] = passwordValidationState

    }




fun firebaseSignIn(email: String , password: String){
   viewModelScope.launch {
       authenticatingUseCases.firebaseSignIn(
           email = email ,
           password = password
       ).collect {
           _signInState.value = it

       }
   }
}

    fun restUser(){
        _signInState.value = Response.Success(false)
    }

}


enum class SignInTextFieldId : TextFieldId {
    EMAIL, PASSWORD
}