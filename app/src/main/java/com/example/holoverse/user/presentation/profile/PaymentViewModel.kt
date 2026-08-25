package com.example.holoverse.user.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import com.example.holoverse.payment.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        observeUserBalance()
    }

    private fun observeUserBalance() {
        viewModelScope.launch {
            preferenceManager.userFlow.collect { user ->
                _uiState.update { it.copy(balance = user?.walletBalance ?: 0.0) }
            }
        }
    }

    fun addFunds(amount: Double) {
        viewModelScope.launch {
            val user = preferenceManager.getUser() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            val response = paymentRepository.depositFunds(user.userId!!, amount)
            when (response) {
                is Response.Success -> {
                    // Update user in preferences to trigger flow
                    val updatedUser = when (user) {
                        is User.Student -> user.copy(walletBalance = user.walletBalance + amount)
                        is User.Mentor -> user.copy(walletBalance = user.walletBalance + amount)
                        is User.Admin -> user.copy(walletBalance = user.walletBalance + amount)
                    }
                    preferenceManager.saveUser(updatedUser)
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Response.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
                else -> {}
            }
        }
    }
}
