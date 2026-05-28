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
import kotlinx.coroutines.async
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

            // Start fetching courses in parallel with the profile
            loadMentorCourses(mentorId)

            val currentUser = authRepository.getCachedUser()

            // Fetch mentor and following status in parallel
            val mentorDeferred = async { fetchDataRepository.fetchMentorById(mentorId) }
            val isFollowingDeferred = async {
                if (currentUser?.userId != null) {
                    authRepository.isFollowing(currentUser.userId!!, mentorId)
                } else {
                    false
                }
            }

            val mentor = mentorDeferred.await()
            val isFollowing = isFollowingDeferred.await()

            val savedCourseIds = when (currentUser) {
                is User.Student -> currentUser.savedCourses ?: emptyList()
                is User.Mentor -> currentUser.savedCourses ?: emptyList()
                else -> emptyList()
            }

            if (mentor != null) {
                _uiState.update {
                    it.copy(
                        mentor = mentor,
                        savedCourseIds = savedCourseIds,
                        isFollowing = isFollowing,
                        isLoading = false,
                        isUserLoggedIn = currentUser != null,
                        isOwnProfile = currentUser?.userId == mentorId
                    )
                }
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
            _uiState.update { it.copy(isFollowLoading = true) }
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
                    state.copy(
                        isFollowing = !isFollowing,
                        mentor = updatedMentor,
                        isFollowLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isFollowLoading = false) }
            }
        }
    }

    private fun loadMentorCourses(mentorId: String) {
        viewModelScope.launch {
            courseRepo.getCoursesByInstructorId(mentorId).collect { response ->
                when (response) {
                    is Response.Success -> {
                        _uiState.update { it.copy(courses = response.data) }
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

    fun toggleSaveCourse(courseId: String) {
        val userId = authRepository.getCachedUser()?.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(savingCourseIds = it.savingCourseIds + courseId) }

            val response = authRepository.toggleSaveCourse(userId, courseId)

            if (response is Response.Success) {
                _uiState.update { state ->
                    val isSaved = state.savedCourseIds.contains(courseId)
                    val newSavedIds = if (isSaved) {
                        state.savedCourseIds - courseId
                    } else {
                        state.savedCourseIds + courseId
                    }
                    state.copy(
                        savedCourseIds = newSavedIds,
                        savingCourseIds = state.savingCourseIds - courseId
                    )
                }
            } else {
                _uiState.update { it.copy(savingCourseIds = it.savingCourseIds - courseId) }
            }
        }
    }
}

data class MentorProfileUiState(
    val mentor: User.Mentor? = null,
    val courses: List<Courses> = emptyList(),
    val savedCourseIds: List<String> = emptyList(),
    val savingCourseIds: Set<String> = emptySet(),
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isOwnProfile: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
