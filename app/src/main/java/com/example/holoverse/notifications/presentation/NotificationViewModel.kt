package com.example.holoverse.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.notifications.domain.models.Notification
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepo: NotificationRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<Response<List<Notification>>>(Response.Loading)
    val notifications: StateFlow<Response<List<Notification>>> = _notifications

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            val user = authRepo.getCurrentUser()
            user?.userId?.let { userId ->
                notificationRepo.getNotifications(userId).collectLatest { response ->
                    _notifications.value = response
                }
            } ?: run {
                _notifications.value = Response.Error("User not authenticated")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepo.markAsRead(notificationId)
        }
    }
}

