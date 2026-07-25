package com.example.holoverse.course.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.core.utils.PreferenceManager
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
    private val fetchDataRepository: FetchDataRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
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

                val rawStudents = fetchDataRepository.fetchStudentsByIds(idsToFetch)
                val targetLang = preferenceManager.getLanguage() ?: "en"
                val students = if (targetLang != "en") {
                    rawStudents.map { student ->
                        student.copy(
                            fullName = student.fullName?.let { translationManager.translate(it, targetLang = targetLang) }
                        )
                    }
                } else {
                    rawStudents
                }
                _uiState.update { it.copy(isLoading = false, students = students) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not authorized") }
            }
        }
    }
}

