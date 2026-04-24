package com.example.holoverse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class HomeTab {
    Explore, YourCourses
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val courses: List<Courses> = emptyList(),
    val allCourses: List<Courses> = emptyList(),
    val recommendedCourses: List<Courses> = emptyList(),
    val mentors: List<User.Mentor> = emptyList(),
    val allMentors: List<User.Mentor> = emptyList(),
    val recommendedMentors: List<User.Mentor> = emptyList(),
    val selectedTab: HomeTab = HomeTab.Explore,
    val selectedCategory: String = "All",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(currentUser = authRepository.getCachedUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    private fun fetchHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch user info from cache
                val user = authRepository.getCachedUser()
                
                // Fetch courses and mentors
                val courses = fetchDataRepository.fetchCourses(forceRefresh)
                val rawMentors = fetchDataRepository.fetchMentors(forceRefresh)

                // Move heavy processing to Background thread to avoid UI jank
                val processedData = withContext(Dispatchers.Default) {
                    val mentors = rawMentors.map { mentor ->
                        if (mentor.profileImageUrl != null && !mentor.profileImageUrl.startsWith("http")) {
                            mentor.copy(profileImageUrl = cloudinaryRepository.getPhotoUrl(mentor.profileImageUrl))
                        } else {
                            mentor
                        }
                    }

                    val userInterests = when (user) {
                        is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
                        is User.Mentor -> user.subjects ?: emptyList()
                        else -> emptyList()
                    }.distinct()

                    val recommendedCourses = courses.filter { course ->
                        userInterests.any { fav ->
                            val nFav = fav.trim().replace("_", " ")
                            val nCat = course.category.trim().replace("_", " ")
                            
                            if (nCat.contains(nFav, ignoreCase = true) || nFav.contains(nCat, ignoreCase = true)) return@any true
                            
                            val catEnum = com.example.holoverse.auth.domain.entities.MentorCategory.entries.find { 
                                it.name.replace("_", " ").equals(nCat, ignoreCase = true) ||
                                it.name.equals(course.category.trim(), ignoreCase = true)
                            }
                            catEnum?.specializations?.any { spec ->
                                spec.replace("_", " ").contains(nFav, ignoreCase = true) ||
                                nFav.contains(spec.replace("_", " "), ignoreCase = true)
                            } == true
                        }
                    }

                    val recommendedMentors = mentors.filter { mentor ->
                        userInterests.any { fav ->
                            val nFav = fav.trim().replace("_", " ")
                            val nSpecName = mentor.specialization.name.replace("_", " ")
                            
                            nSpecName.contains(nFav, ignoreCase = true) ||
                            nFav.contains(nSpecName, ignoreCase = true) ||
                            mentor.specialization.specializations.any { spec -> 
                                spec.replace("_", " ").contains(nFav, ignoreCase = true) || 
                                nFav.contains(spec.replace("_", " "), ignoreCase = true)
                            }
                        }
                    }
                    
                    Triple(mentors, recommendedCourses, recommendedMentors)
                }

                val (mentors, recommendedCourses, recommendedMentors) = processedData

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        allCourses = courses,
                        recommendedCourses = recommendedCourses,
                        allMentors = mentors,
                        recommendedMentors = recommendedMentors
                    )
                }
                // Apply filter after data is loaded
                applyFilter(_uiState.value.selectedCategory)

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

    fun onCategorySelected(category: String) {
        applyFilter(category)
    }

    private fun applyFilter(category: String) {
        _uiState.update { state ->
            val filteredCourses = if (category == "All") {
                state.allCourses
            } else {
                state.allCourses.filter { it.category == category }
            }
            
            val filteredMentors = if (category == "All") {
                state.allMentors
            } else {
                state.allMentors.filter { mentor ->
                    mentor.specialization.name.replace("_", " ").equals(category, ignoreCase = true) ||
                    mentor.specialization.specializations.any { it.equals(category, ignoreCase = true) }
                }
            }
            
            state.copy(
                selectedCategory = category,
                courses = filteredCourses,
                mentors = filteredMentors
            )
        }
    }
}
