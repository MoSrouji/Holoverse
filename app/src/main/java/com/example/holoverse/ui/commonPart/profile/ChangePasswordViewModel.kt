package com.example.holoverse.ui.commonPart.profile

import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.ui.commonPart.auth.presentaiton.base.BaseValidationViewModel
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonPart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseValidationViewModel() {

    private val _changePasswordState = MutableStateFlow<Response<Boolean>>(Response.Success(false))
    val changePasswordState: StateFlow<Response<Boolean>> = _changePasswordState.asStateFlow()

    init {
        forms[ChangePasswordTextFieldId.OLD_PASSWORD] = ValidationState(
            type = TextFieldType.Password,
            id = ChangePasswordTextFieldId.OLD_PASSWORD
        )
        forms[ChangePasswordTextFieldId.NEW_PASSWORD] = ValidationState(
            type = TextFieldType.Password,
            id = ChangePasswordTextFieldId.NEW_PASSWORD
        )
        forms[ChangePasswordTextFieldId.CONFIRM_PASSWORD] = ValidationState(
            type = TextFieldType.Password,
            id = ChangePasswordTextFieldId.CONFIRM_PASSWORD
        )
    }

    fun changePassword() {
        val oldPassword = forms[ChangePasswordTextFieldId.OLD_PASSWORD]?.text ?: ""
        val newPassword = forms[ChangePasswordTextFieldId.NEW_PASSWORD]?.text ?: ""
        val confirmPassword = forms[ChangePasswordTextFieldId.CONFIRM_PASSWORD]?.text ?: ""

        if (newPassword != confirmPassword) {
            // Manually set error for confirm password
            forms[ChangePasswordTextFieldId.CONFIRM_PASSWORD] = forms[ChangePasswordTextFieldId.CONFIRM_PASSWORD]!!.copy(
                hasError = true,
                errorMessageId = com.example.holoverse.R.string.passwords_do_not_match
            )
            return
        }

        viewModelScope.launch {
            authRepository.changePassword(oldPassword, newPassword).collect { response ->
                _changePasswordState.value = response
            }
        }
    }
}

enum class ChangePasswordTextFieldId : TextFieldId {
    OLD_PASSWORD, NEW_PASSWORD, CONFIRM_PASSWORD
}
