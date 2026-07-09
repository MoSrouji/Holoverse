package com.example.holoverse.ui.teacherpart.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.fetch.domain.FetchDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StudentListTab {
    Followers, Following
}

data class StudentListUiState(
    val isLoading: Boolean = false,
    val students: List<User.Student> = emptyList(),
    val selectedTab: StudentListTab = StudentListTab.Followers,
    val error: String? = null
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fetchDataRepository: FetchDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentListUiState())
    val uiState: StateFlow<StudentListUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    fun onTabSelected(tab: StudentListTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is User.Mentor) {
                val idsToFetch = if (_uiState.value.selectedTab == StudentListTab.Followers) {
                    currentUser.followers ?: emptyList()
                } else {
                    currentUser.following ?: emptyList()
                }
                
                if (idsToFetch.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, students = emptyList()) }
                    return@launch
                }

                val students = fetchDataRepository.fetchStudentsByIds(idsToFetch)
                _uiState.update { it.copy(isLoading = false, students = students) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not authorized") }
            }
        }
    }
}
