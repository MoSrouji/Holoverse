package com.example.holoverse.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.admin.domain.repository.AdminRepository
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val allUsers: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedUserType: UserType? = null,
    val error: String? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow<UserType?>(null)
    private val _users = MutableStateFlow<List<User>>(emptyList())

    val state: StateFlow<UserManagementUiState> = combine(
        _users, _searchQuery, _selectedType, _uiState
    ) { users, query, type, currentState ->
        val filtered = users.filter { user ->
            val matchesQuery = user.fullName?.contains(query, ignoreCase = true) == true ||
                    user.email?.contains(query, ignoreCase = true) == true
            val matchesType = type == null || user.accountType == type
            matchesQuery && matchesType
        }
        currentState.copy(
            allUsers = users,
            filteredUsers = filtered,
            searchQuery = query,
            selectedUserType = type
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserManagementUiState()
    )

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            adminRepository.getAllUsers().collect { response ->
                when (response) {
                    is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Response.Success -> {
                        _users.value = response.data
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is Response.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = response.message) }
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onUserTypeFilterChange(type: UserType?) {
        _selectedType.value = type
    }
}

private fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> = this
