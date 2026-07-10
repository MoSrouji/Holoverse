package com.example.holoverse.ui.mentor.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MentorAnalysisUiState(
    val mentor: User.Mentor? = null,
    val courses: List<Courses> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalStudents: Int = 0,
    val totalFollowers: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageRating: Double = 0.0,
    val averageCompletionRate: Double = 0.0,
    val averageProgress: Double = 0.0
)

@HiltViewModel
class MentorAnalysisViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepo: CourseRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorAnalysisUiState())
    val uiState: StateFlow<MentorAnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is User.Mentor) {
                _uiState.update { it.copy(mentor = currentUser) }
                fetchCourses(currentUser.userId ?: "")
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found or not a mentor") }
            }
        }
    }

    private suspend fun fetchCourses(mentorId: String) {
        courseRepo.getCoursesByInstructorId(mentorId).collectLatest { response ->
            when (response) {
                is Response.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Response.Success -> {
                    val courses = response.data ?: emptyList()
                    calculateStats(courses)
                }
                is Response.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            }
        }
    }

    private fun calculateStats(courses: List<Courses>) {
        val mentor = _uiState.value.mentor
        val totalStudents = mentor?.totalStudentsTaught ?: courses.sumOf { it.numEnrolled }
        val totalFollowers = mentor?.followersCount ?: mentor?.followers?.size ?: 0
        val totalRevenue = courses.sumOf { it.price * it.numEnrolled }
        val avgRating = if (courses.isNotEmpty()) courses.sumOf { it.rating } / courses.size else 0.0
        val avgCompletion = if (courses.isNotEmpty()) courses.sumOf { it.completionRate } / courses.size else 0.0
        val avgProgress = if (courses.isNotEmpty()) courses.sumOf { it.averageProgress } / courses.size else 0.0

        _uiState.update { 
            it.copy(
                courses = courses.sortedByDescending { c -> c.numEnrolled },
                isLoading = false,
                totalStudents = totalStudents,
                totalFollowers = totalFollowers,
                totalRevenue = totalRevenue,
                averageRating = mentor?.averageRating ?: avgRating,
                averageCompletionRate = avgCompletion,
                averageProgress = avgProgress
            )
        }
    }
}

