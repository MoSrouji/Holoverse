package com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.use_cases.AuthUseCases
import com.example.holoverse.ui.commonPart.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import com.example.holoverse.utils.PreferenceManager
import com.example.holoverse.utils.Response
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

    private var emailValidationState =
        ValidationState(id = SignInTextFieldId.EMAIL, type = TextFieldType.Email)

    private var passwordValidationState =
        ValidationState(id = SignInTextFieldId.PASSWORD, type = TextFieldType.Password)


    init {
        forms[SignInTextFieldId.EMAIL] = emailValidationState
        forms[SignInTextFieldId.PASSWORD] = passwordValidationState
        checkUserSession()
    }

    private fun checkUserSession() {
        val user = preferenceManager.getUser()
        if (user != null) {
            _signInState.value = Response.Success(true)
        }
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
                user?.let { preferenceManager.saveUser(it) }
            }
        }.launchIn(viewModelScope)
    }

    fun restUser() {
        _signInState.value = Response.Success(false)
    }

}


enum class SignInTextFieldId : TextFieldId {
    EMAIL, PASSWORD
}
