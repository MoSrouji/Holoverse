package com.example.holoverse.user.presentation.help_center

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HelpCenterUiState(
    val faqs: List<FaqItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val supportChatId: String? = null,
    val currentUser: User? = null
)

data class FaqItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

@HiltViewModel
class HelpCenterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpCenterUiState())
    val uiState: StateFlow<HelpCenterUiState> = _uiState.asStateFlow()

    init {
        loadFaqs()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCachedUser() ?: authRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun loadFaqs() {
        val faqs = listOf(
            FaqItem("How do I book a session?", "Go to the mentor's profile and click on 'Book Session' to see their availability."),
            FaqItem("How can I change my password?", "Navigate to Profile > Edit Profile > Change Password."),
            FaqItem("What is the refund policy?", "Refunds are processed if a session is cancelled at least 24 hours in advance."),
            FaqItem("How do I become a mentor?", "Go to the profile setup and select 'Mentor' as your user type during registration."),
            FaqItem("Can I chat with my mentor?", "Yes, you can initiate a chat from the mentor's profile or through the Messages tab.")
        )
        _uiState.update { it.copy(faqs = faqs) }
    }

    fun onFaqClick(index: Int) {
        _uiState.update { state ->
            val updatedFaqs = state.faqs.toMutableList()
            val item = updatedFaqs[index]
            updatedFaqs[index] = item.copy(isExpanded = !item.isExpanded)
            state.copy(faqs = updatedFaqs)
        }
    }

    fun contactSupport(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            authRepository.getSupportAdmin().collectLatest { response ->
                when (response) {
                    is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Response.Success -> {
                        val admin = response.data
                        chatRepository.createOrGetSupportChat(
                            userId = user.userId ?: "",
                            userName = user.fullName ?: "User",
                            userImageUrl = when(user) {
                                is User.Student -> user.profileImageUrl
                                is User.Mentor -> user.profileImageUrl
                                is User.Admin -> user.profileImageUrl
                            },
                            admin = admin
                        ).collectLatest { chatResponse ->
                            when (chatResponse) {
                                is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
                                is Response.Success -> {
                                    _uiState.update { it.copy(isLoading = false, supportChatId = chatResponse.data) }
                                    onSuccess(chatResponse.data)
                                }
                                is Response.Error -> {
                                    _uiState.update { it.copy(isLoading = false, error = chatResponse.message) }
                                }
                            }
                        }
                    }
                    is Response.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = response.message) }
                    }
                }
            }
        }
    }
}
