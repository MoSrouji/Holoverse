package com.example.holoverse.ui.commonPart.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import com.example.holoverse.utils.LanguageManager
import com.example.holoverse.utils.PreferenceManager
import com.example.holoverse.utils.Response
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
    val selectedThemeMode: String = "system",
    val isSignedOut: Boolean = false
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
        loadUserProfile()
        loadThemeMode()
    }

    fun updateLanguageName(context: Context) {
        _uiState.update { 
            it.copy(selectedLanguageName = languageManager.getSelectedLanguageName(context))
        }
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val user = preferenceManager.getUser()
        
        if (user != null) {
            val rawImageUrl = when (user) {
                is User.Student -> user.profileImageUrl
                is User.Mentor -> null
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
                        _uiState.update { it.copy(isLoading = false, isSignedOut = true) }
                    }
                    is Response.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = "Error") }
                    }
                }
            }
        }
    }

    fun onRefresh() {
        loadUserProfile()
    }
}
