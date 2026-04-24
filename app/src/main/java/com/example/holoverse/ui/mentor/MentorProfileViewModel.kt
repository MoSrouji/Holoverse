package com.example.holoverse.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.data.CourseRepo
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MentorProfileViewModel @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val courseRepo: CourseRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMentorProfile(mentorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val mentor = fetchDataRepository.fetchMentorById(mentorId)
            if (mentor != null) {
                _uiState.update { it.copy(mentor = mentor, isLoading = false) }
                loadMentorCourses(mentorId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Mentor not found") }
            }
        }
    }

    private fun loadMentorCourses(mentorId: String) {
        viewModelScope.launch {
            courseRepo.getCoursesByInstructorId(mentorId).collect { response ->
                when (response) {
                    is Response.Success -> {
                        _uiState.update { it.copy(courses = response.data ?: emptyList()) }
                    }
                    is Response.Error -> {
                        // Handle error if needed
                    }
                    Response.Loading -> {
                        // Already loading main profile
                    }
                }
            }
        }
    }
}

data class MentorProfileUiState(
    val mentor: User.Mentor? = null,
    val courses: List<Courses> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
