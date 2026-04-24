package com.example.holoverse.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
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
    private val courseRepo: CourseRepo,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMentorProfile(mentorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = authRepository.getCachedUser()
            val mentor = fetchDataRepository.fetchMentorById(mentorId)

            if (mentor != null) {
                val isFollowing = if (currentUser?.userId != null) {
                    authRepository.isFollowing(currentUser.userId!!, mentorId)
                } else {
                    false
                }

                _uiState.update {
                    it.copy(
                        mentor = mentor,
                        isFollowing = isFollowing,
                        isLoading = false,
                        isUserLoggedIn = currentUser != null,
                        isOwnProfile = currentUser?.userId == mentorId
                    )
                }
                loadMentorCourses(mentorId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Mentor not found") }
            }
        }
    }

    fun toggleFollow() {
        val mentorId = _uiState.value.mentor?.userId ?: return
        val currentUser = authRepository.getCachedUser() ?: return
        val followerId = currentUser.userId ?: return
        val isFollowing = _uiState.value.isFollowing

        viewModelScope.launch {
            val result = if (isFollowing) {
                authRepository.unfollowMentor(followerId, mentorId)
            } else {
                authRepository.followMentor(followerId, mentorId)
            }

            if (result is Response.Success) {
                _uiState.update { state ->
                    val updatedMentor = state.mentor?.let { m ->
                        if (isFollowing) {
                            m.copy(
                                followersCount = (m.followersCount ?: 1) - 1,
                                followers = m.followers?.filter { it != followerId }
                            )
                        } else {
                            m.copy(
                                followersCount = (m.followersCount ?: 0) + 1,
                                followers = (m.followers ?: emptyList()) + followerId
                            )
                        }
                    }
                    state.copy(isFollowing = !isFollowing, mentor = updatedMentor)
                }
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
    val isFollowing: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isOwnProfile: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
