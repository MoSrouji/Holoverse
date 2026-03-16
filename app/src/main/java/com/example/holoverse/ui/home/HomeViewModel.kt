package com.example.holoverse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab {
    Explore, YourCourses
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val courses: List<Courses> = emptyList(),
    val mentors: List<User.Mentor> = emptyList(),
    val selectedTab: HomeTab = HomeTab.Explore,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    private fun fetchHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch user info for the greeting
                val user = authRepository.getCurrentUser()
                
                // Fetch courses and mentors
                val courses = fetchDataRepository.fetchCourses(forceRefresh)
                val mentors = fetchDataRepository.fetchMentors(forceRefresh)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        courses = courses,
                        mentors = mentors
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to fetch home data"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        fetchHomeData(forceRefresh = true)
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
