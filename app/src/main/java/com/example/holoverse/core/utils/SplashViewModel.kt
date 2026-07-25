package com.example.holoverse.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val currentUser: StateFlow<User?> = preferenceManager.userFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = preferenceManager.getUser()
        )

    val isProfileComplete: StateFlow<Boolean> = preferenceManager.userFlow
        .map { preferenceManager.isProfileComplete() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = preferenceManager.isProfileComplete()
        )

    val themeMode: StateFlow<String> = preferenceManager.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = preferenceManager.getThemeMode()
        )

    init {
        viewModelScope.launch {
            // Wait for first non-null or null emission to be sure we have the latest state
            _isLoading.value = false
        }
    }
}

