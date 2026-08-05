package com.example.holoverse.auth.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
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
class SignInViewModel @Inject constructor(
    private val authenticatingUseCases: AuthUseCases,
    private val preferenceManager: PreferenceManager
) : BaseValidationViewModel() {
    private val _signInState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signInState: State<Response<Boolean>> = _signInState

    private val _userType = mutableStateOf<UserType?>(null)
    val userType: State<UserType?> = _userType

    private var emailValidationState =
        ValidationState(id = SignInTextFieldId.EMAIL, type = TextFieldType.Email)

    private var passwordValidationState =
        ValidationState(id = SignInTextFieldId.PASSWORD, type = TextFieldType.Password)


    init {
        forms[SignInTextFieldId.EMAIL] = emailValidationState
        forms[SignInTextFieldId.PASSWORD] = passwordValidationState
        android.util.Log.d("SignInViewModel", "init: SignInViewModel created ${hashCode()}")
    }


    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("SignInViewModel", "onCleared: SignInViewModel destroyed ${hashCode()}")
    }


    suspend fun firebaseSignIn(email: String, password: String) {
        authenticatingUseCases.firebaseSignIn(
            email = email,
            password = password
        ).onEach { response ->
            _signInState.value = response
            if (response is Response.Success && response.data) {
                // Fetch and save user to preferences after successful sign in
                val user = authenticatingUseCases.getCurrentUser()
                user?.let {
                    preferenceManager.saveUser(it)
                    _userType.value = it.accountType
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        clearForms()
        _signInState.value = Response.Success(false)
        _userType.value = null
        // Re-initialize mandatory forms
        forms[SignInTextFieldId.EMAIL] = emailValidationState
        forms[SignInTextFieldId.PASSWORD] = passwordValidationState
    }

}


enum class SignInTextFieldId : TextFieldId {
    EMAIL, PASSWORD
}

