package com.example.holoverse.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    val themeMode: StateFlow<String> = preferenceManager.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = preferenceManager.getThemeMode()
        )

    init {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                preferenceManager.getUser()
            }
            _currentUser.value = user
            _isLoading.value = false
        }
    }
}
