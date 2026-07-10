package com.example.holoverse.ui.mentor.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnnouncementsUiState(
    val isLoading: Boolean = false,
    val courses: List<Courses> = emptyList(),
    val boostedCourses: List<BoostedCourse> = emptyList(),
    val error: String? = null,
    val boostResponse: Response<Boolean>? = null
)

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val courseRepo: CourseRepo,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentUser = authRepo.getCurrentUser()
            val mentorId = currentUser?.userId ?: ""

            if (mentorId.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Mentor not found") }
                return@launch
            }

            combine(
                courseRepo.getCoursesByInstructorId(mentorId),
                courseRepo.getBoostedCourses()
            ) { coursesResponse, boostedResponse ->
                val isLoading = coursesResponse is Response.Loading || boostedResponse is Response.Loading
                val error = when {
                    coursesResponse is Response.Error -> coursesResponse.message
                    boostedResponse is Response.Error -> boostedResponse.message
                    else -> null
                }
                
                val courses = if (coursesResponse is Response.Success) coursesResponse.data ?: emptyList() else _uiState.value.courses
                val boosted = if (boostedResponse is Response.Success) boostedResponse.data ?: emptyList() else _uiState.value.boostedCourses

                _uiState.update { 
                    it.copy(
                        isLoading = isLoading,
                        courses = courses,
                        boostedCourses = boosted,
                        error = error
                    )
                }
            }.collectLatest { }
        }
    }

    fun boostCourse(boostedCourse: BoostedCourse) {
        viewModelScope.launch {
            _uiState.update { it.copy(boostResponse = Response.Loading) }
            courseRepo.boostCourse(boostedCourse).collectLatest { response ->
                _uiState.update { it.copy(boostResponse = response) }
                if (response is Response.Success) {
                    loadData() // Refresh data to show boosted status
                }
            }
        }
    }

    fun resetBoostResponse() {
        _uiState.update { it.copy(boostResponse = null) }
    }
}

