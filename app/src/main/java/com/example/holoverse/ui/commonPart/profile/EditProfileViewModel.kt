package com.example.holoverse.ui.commonpart.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import com.example.holoverse.auth.presentation.common.base.BaseValidationViewModel
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.event.ValidationEvent
import com.example.holoverse.auth.presentation.common.validation.event.ValidationResultEvent
import com.example.holoverse.auth.presentation.common.validation.interfaces.TextFieldId
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.utils.PreferenceManager
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : BaseValidationViewModel() {

    private val _editProfileState = MutableStateFlow<Response<Boolean>>(Response.Success(false))
    val editProfileState: StateFlow<Response<Boolean>> = _editProfileState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedGender by mutableStateOf("Select Your Gender")

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = preferenceManager.getUser()
        _currentUser.value = user
        user?.let {
            setupInitialForms(it)
        }
    }

    private fun setupInitialForms(user: User) {
        forms[EditProfileTextFieldId.FULL_NAME] = ValidationState(
            text = user.fullName ?: "",
            type = TextFieldType.Text,
            id = EditProfileTextFieldId.FULL_NAME
        )
        
        forms[EditProfileTextFieldId.EMAIL] = ValidationState(
            text = user.email ?: "",
            type = TextFieldType.Email,
            id = EditProfileTextFieldId.EMAIL
        )

        val (phone, address, gender, dob, bio) = when (user) {
            is User.Student -> listOf(
                user.phoneNumber ?: "",
                user.address ?: "",
                user.gender ?: "Select Your Gender",
                user.dateOfBirth ?: "",
                ""
            )
            is User.Mentor -> listOf(
                user.phoneNumber ?: "",
                user.address ?: "",
                user.gender ?: "Select Your Gender",
                user.dateOfBirth ?: "",
                user.bio ?: ""
            )
        }

        forms[EditProfileTextFieldId.PHONE_NUMBER] = ValidationState(
            text = phone,
            type = TextFieldType.PhoneNumber,
            id = EditProfileTextFieldId.PHONE_NUMBER
        )
        forms[EditProfileTextFieldId.ADDRESS] = ValidationState(
            text = address,
            type = TextFieldType.Text,
            id = EditProfileTextFieldId.ADDRESS
        )
        forms[EditProfileTextFieldId.DATE_OF_BIRTH] = ValidationState(
            text = dob,
            type = TextFieldType.Text,
            id = EditProfileTextFieldId.DATE_OF_BIRTH
        )
        forms[EditProfileTextFieldId.GENDER] = ValidationState(
            text = if (gender == "Select Your Gender") "" else gender,
            type = TextFieldType.Text,
            id = EditProfileTextFieldId.GENDER
        )
        
        selectedGender = gender

        if (user is User.Mentor) {
            forms[EditProfileTextFieldId.BIO] = ValidationState(
                text = bio,
                type = TextFieldType.Text,
                id = EditProfileTextFieldId.BIO
            )
        }
    }

    fun onDateSelected(millis: Long) {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val dateString = formatter.format(java.util.Date(millis))
        onEvent(
            ValidationEvent.TextFieldValueChange(
                forms[EditProfileTextFieldId.DATE_OF_BIRTH]!!.copy(
                    text = dateString,
                    hasError = false,
                    errorMessageId = null
                )
            )
        )
    }

    fun updateProfile() {
        viewModelScope.launch {
            _editProfileState.value = Response.Loading
            
            try {
                val currentUserSnapshot = _currentUser.value ?: return@launch
                val newEmail = forms[EditProfileTextFieldId.EMAIL]?.text ?: currentUserSnapshot.email ?: ""
                val emailChanged = newEmail != currentUserSnapshot.email

                if (emailChanged) {
                    authRepository.updateEmail(newEmail).collect { response ->
                        if (response is Response.Error) {
                            _editProfileState.value = response
                            return@collect
                        }
                    }
                }

                val imageUrl = if (selectedImageUri != null) {
                    val uploadResult = cloudinaryRepository.uploadFile(selectedImageUri!!)
                    uploadResult.getOrThrow()
                } else {
                    when (val user = currentUserSnapshot) {
                        is User.Student -> user.profileImageUrl
                        is User.Mentor -> user.profileImageUrl
                        null -> null
                    }
                }

                val updatedUser = when (currentUserSnapshot) {
                    is User.Student -> currentUserSnapshot.copy(
                        fullName = forms[EditProfileTextFieldId.FULL_NAME]?.text,
                        email = newEmail,
                        phoneNumber = forms[EditProfileTextFieldId.PHONE_NUMBER]?.text,
                        address = forms[EditProfileTextFieldId.ADDRESS]?.text,
                        gender = forms[EditProfileTextFieldId.GENDER]?.text,
                        dateOfBirth = forms[EditProfileTextFieldId.DATE_OF_BIRTH]?.text,
                        profileImageUrl = imageUrl
                    )
                    is User.Mentor -> currentUserSnapshot.copy(
                        fullName = forms[EditProfileTextFieldId.FULL_NAME]?.text,
                        email = newEmail,
                        phoneNumber = forms[EditProfileTextFieldId.PHONE_NUMBER]?.text,
                        address = forms[EditProfileTextFieldId.ADDRESS]?.text,
                        gender = forms[EditProfileTextFieldId.GENDER]?.text,
                        dateOfBirth = forms[EditProfileTextFieldId.DATE_OF_BIRTH]?.text,
                        bio = forms[EditProfileTextFieldId.BIO]?.text,
                        profileImageUrl = imageUrl
                    )
                }

                val updateFlow = when (updatedUser) {
                    is User.Student -> authRepository.updateStudentProfile(updatedUser)
                    is User.Mentor -> authRepository.updateMentorProfile(updatedUser)
                }

                updateFlow.collect { response ->
                    _editProfileState.value = response
                }
            } catch (e: Exception) {
                _editProfileState.value = Response.Error(e.message ?: "Update failed")
            }
        }
    }
}

enum class EditProfileTextFieldId : TextFieldId {
    FULL_NAME, EMAIL, PHONE_NUMBER, ADDRESS, GENDER, DATE_OF_BIRTH, BIO
}

