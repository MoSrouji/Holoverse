package com.example.holoverse.user.presentation.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import com.example.holoverse.core.utils.LanguageManager
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val accountType: String = "",
    val error: String? = null,
    val selectedLanguageName: String = "",
    val selectedLanguageCode: String? = null,
    val selectedThemeMode: String = "system"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val cloudinaryRepository: CloudinaryRepository,
    private val languageManager: LanguageManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserChanges()
        loadThemeMode()
        updateLanguageName()
    }

    fun updateLanguageName() {
        _uiState.update {
            it.copy(
                selectedLanguageName = languageManager.getSelectedLanguageName(),
                selectedLanguageCode = preferenceManager.getLanguage()
            )
        }
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            preferenceManager.userFlow.collect { user ->
                updateUiWithUser(user)
            }
        }
    }

    private fun updateUiWithUser(user: User?) {
        if (user != null) {
            val rawImageUrl = when (user) {
                is User.Student -> user.profileImageUrl
                is User.Mentor -> user.profileImageUrl
                is User.Admin -> user.profileImageUrl
            }

            val finalImageUrl = rawImageUrl?.let {
                if (it.startsWith("http")) it else cloudinaryRepository.getPhotoUrl(it)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    fullName = user.fullName ?: "No Name Provided",
                    email = user.email ?: "No Email Provided",
                    profileImageUrl = finalImageUrl,
                    accountType = user.accountType.name,
                    error = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "User profile not found. Please sign in again."
                )
            }
        }
    }

    private fun loadThemeMode() {
        _uiState.update {
            it.copy(selectedThemeMode = preferenceManager.getThemeMode())
        }
    }

    fun onLanguageSelected(languageCode: String?) {
        languageManager.setLocale(languageCode)
        updateLanguageName()
    }

    fun onThemeSelected(themeMode: String) {
        preferenceManager.saveThemeMode(themeMode)
        _uiState.update { it.copy(selectedThemeMode = themeMode) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.firebaseSignOut().collect { response ->
                when (response) {
                    is Response.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Response.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }

                    is Response.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = "Error") }
                    }
                }
            }
        }
    }

    fun onRefresh() {
        updateUiWithUser(preferenceManager.getUser())
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uploadResult = cloudinaryRepository.uploadFile(uri)

            uploadResult.onSuccess { result ->
                val imageUrl = result.url
                Log.d("ProfileViewModel", "Cloudinary upload success. URL: $imageUrl")
                val currentUser = preferenceManager.getUser()
                Log.d("ProfileViewModel", "Current user from preferenceManager: $currentUser")
                if (currentUser != null) {
                    val updatedUser = when (currentUser) {
                        is User.Student -> currentUser.copy(profileImageUrl = imageUrl)
                        is User.Mentor -> currentUser.copy(profileImageUrl = imageUrl)
                        is User.Admin -> currentUser.copy(profileImageUrl = imageUrl)
                    }
                    Log.d("ProfileViewModel", "Updating user in AuthRepository: $updatedUser")

                    val updateFlow = when (updatedUser) {
                        is User.Student -> authRepository.updateStudentProfile(updatedUser)
                        is User.Mentor -> authRepository.updateMentorProfile(updatedUser)
                        is User.Admin -> throw IllegalStateException("Admin profile update not implemented")
                    }

                    updateFlow.collect { response ->
                        when (response) {
                            is Response.Loading -> {
                                Log.d("ProfileViewModel", "Profile update loading...")
                            }

                            is Response.Success -> {
                                Log.d(
                                    "ProfileViewModel",
                                    "Profile update success. UI will update via flow."
                                )
                                _uiState.update { it.copy(isLoading = false) }
                            }

                            is Response.Error -> {
                                Log.e(
                                    "ProfileViewModel",
                                    "Profile update error: $response"
                                )
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = response.toString()
                                    )
                                }
                            }
                        }
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}


