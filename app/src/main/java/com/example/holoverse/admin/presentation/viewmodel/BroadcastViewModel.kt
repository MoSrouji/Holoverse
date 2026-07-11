package com.example.holoverse.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.notifications.domain.repository.BroadcastTarget
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BroadcastUiState(
    val title: String = "",
    val message: String = "",
    val target: BroadcastTarget = BroadcastTarget.ALL,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BroadcastViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BroadcastUiState())
    val uiState: StateFlow<BroadcastUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onMessageChange(newMessage: String) {
        _uiState.update { it.copy(message = newMessage) }
    }

    fun onTargetChange(newTarget: BroadcastTarget) {
        _uiState.update { it.copy(target = newTarget) }
    }

    fun sendBroadcast() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() || currentState.message.isBlank()) {
            _uiState.update { it.copy(error = "Title and message cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val response = notificationRepository.sendBroadcastNotification(
                title = currentState.title,
                body = currentState.message,
                target = currentState.target
            )
            
            when (response) {
                is Response.Success -> {
                    _uiState.update { 
                        it.copy(isLoading = false, isSuccess = true, title = "", message = "") 
                    }
                }
                is Response.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
                is Response.Loading -> {}
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
