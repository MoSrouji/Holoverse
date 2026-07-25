package com.example.holoverse.home.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

enum class HomeTab {
    Explore, YourCourses
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val courses: List<Courses> = emptyList(),
    val allCourses: List<Courses> = emptyList(),
    val enrolledCourses: List<Courses> = emptyList(),
    val savedCourses: List<Courses> = emptyList(),
    val categories: List<AppCategory> = listOf(AppCategory.OTHER),
    val recommendedCourses: List<Courses> = emptyList(),
    val mentors: List<User.Mentor> = emptyList(),
    val allMentors: List<User.Mentor> = emptyList(),
    val recommendedMentors: List<User.Mentor> = emptyList(),
    val boostedCourses: List<BoostedCourse> = emptyList(),
    val savingCourseIds: Set<String> = emptySet(),
    val selectedTab: HomeTab = HomeTab.Explore,
    val selectedCategory: AppCategory = AppCategory.OTHER,
    val error: String? = null,
    val isOffline: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    private val preferenceManager: PreferenceManager,
    private val translationManager: TranslationManager,
    private val application: Application
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(HomeUiState(currentUser = authRepository.getCachedUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserChanges()
        fetchHomeData()
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            preferenceManager.userFlow.collect { user ->
                val currentUser = _uiState.value.currentUser
                if (user?.userId != currentUser?.userId) {
                    _uiState.update { it.copy(currentUser = user) }
                    // If user changed, we should probably re-fetch data to get new recommendations
                    if (currentUser != null && user != null) {
                        fetchHomeData(forceRefresh = false)
                    }
                } else if (user != currentUser) {
                    // Same user but fields updated (e.g. name, profile image)
                    _uiState.update { it.copy(currentUser = user) }
                }
            }
        }
    }

    private fun fetchHomeData(forceRefresh: Boolean = false, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                // Cleanup expired boosts first
                fetchDataRepository.cleanupExpiredBoosts()

                // Start fetching courses, mentors, and boosted courses in parallel
                val coursesDeferred = async { fetchDataRepository.fetchCourses(forceRefresh) }
                val mentorsDeferred = async { fetchDataRepository.fetchMentors(forceRefresh) }
                val boostedCoursesDeferred = async { fetchDataRepository.fetchBoostedCourses() }

                // Fetch user info from cache
                val user = authRepository.getCachedUser()

                // Await results
                val rawCourses = coursesDeferred.await()
                val rawMentors = mentorsDeferred.await()
                val rawBoostedCourses = boostedCoursesDeferred.await()

                // Move heavy processing to Background thread to avoid UI jank
                val processedData = withContext(Dispatchers.Default) {
                    val targetLang = preferenceManager.getLanguage() ?: "en"

                    val mentors = rawMentors.map { mentor ->
                        val mentorWithUrl = if (mentor.profileImageUrl != null && !mentor.profileImageUrl.startsWith("http")) {
                            mentor.copy(profileImageUrl = cloudinaryRepository.getPhotoUrl(mentor.profileImageUrl))
                        } else {
                            mentor
                        }

                        if (targetLang != "en") {
                            mentorWithUrl.copy(
                                fullName = mentorWithUrl.fullName?.let { translationManager.translate(it, targetLang = targetLang) },
                                bio = mentorWithUrl.bio?.let { translationManager.translate(it, targetLang = targetLang) },
                                certifications = mentorWithUrl.certifications?.let { translationManager.translate(it, targetLang = targetLang) }
                            )
                        } else {
                            mentorWithUrl
                        }
                    }

                    val translatedCourses = if (targetLang != "en") {
                        rawCourses.map { course ->
                            course.copy(
                                name = translationManager.translate(course.name, targetLang = targetLang),
                                description = translationManager.translate(course.description, targetLang = targetLang),
                                instructorName = translationManager.translate(course.instructorName, targetLang = targetLang)
                            )
                        }
                    } else {
                        rawCourses
                    }

                    val boostedCourses = if (targetLang != "en") {
                        rawBoostedCourses.map { boost ->
                            boost.copy(
                                courseName = translationManager.translate(boost.courseName, targetLang = targetLang),
                                courseDescription = translationManager.translate(boost.courseDescription, targetLang = targetLang),
                                instructorName = translationManager.translate(boost.instructorName, targetLang = targetLang)
                            )
                        }
                    } else {
                        rawBoostedCourses
                    }

                    val userInterests = when (user) {
                        is User.Student -> (user.favouriteSubjects
                            ?: emptyList()) + (user.academicInterests ?: emptyList())

                        is User.Mentor -> user.subjects ?: emptyList()
                        else -> emptyList()
                    }.distinct()

                    val recommendedCourses = translatedCourses.filter { course ->
                        userInterests.any { fav ->
                            val nFav = fav.trim().replace("_", " ").uppercase()
                            val nCat = course.category.name

                            if (nCat.contains(nFav, ignoreCase = true) || nFav.contains(
                                    nCat,
                                    ignoreCase = true
                                )
                            ) return@any true

                            course.category.specializations.any { specRes ->
                                val spec = application.getString(specRes)
                                spec.replace("_", " ").uppercase()
                                    .contains(nFav, ignoreCase = true) ||
                                        nFav.contains(
                                            spec.replace("_", " ").uppercase(),
                                            ignoreCase = true
                                        )
                            } == true
                        }
                    }

                    val recommendedMentors = mentors.filter { mentor ->
                        userInterests.any { fav ->
                            val nFav = fav.trim().replace("_", " ").uppercase()
                            val nSpecName = mentor.specialization.name

                            nSpecName.contains(nFav, ignoreCase = true) ||
                                    nFav.contains(nSpecName, ignoreCase = true) ||
                                    mentor.specialization.specializations.any { specRes ->
                                        val spec = application.getString(specRes)
                                        spec.replace("_", " ").uppercase()
                                            .contains(nFav, ignoreCase = true) ||
                                                nFav.contains(
                                                    spec.replace("_", " ").uppercase(),
                                                    ignoreCase = true
                                                )
                                    }
                        }
                    }

                    Triple(mentors, translatedCourses, boostedCourses) to (recommendedCourses to recommendedMentors)
                }

                val (mainData, recommendedData) = processedData
                val (mentors, courses, boostedCourses) = mainData
                val (recommendedCourses, recommendedMentors) = recommendedData

                val enrolledCoursesIds = when (user) {
                    is User.Student -> user.enrolledCourses ?: emptyList()
                    is User.Mentor -> user.enrolledCourses ?: emptyList()
                    else -> emptyList()
                }
                val enrolledCourses = courses.filter { it.id in enrolledCoursesIds }

                val savedCoursesIds = when (user) {
                    is User.Student -> user.savedCourses ?: emptyList()
                    is User.Mentor -> user.savedCourses ?: emptyList()
                    else -> emptyList()
                }
                val savedCourses = courses.filter { it.id in savedCoursesIds }

                val categories = listOf(AppCategory.OTHER) + courses
                    .map { it.category }
                    .distinct()
                    .filter { it != AppCategory.OTHER }
                    .sortedBy { it.name }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        allCourses = courses,
                        enrolledCourses = enrolledCourses,
                        savedCourses = savedCourses,
                        categories = categories,
                        recommendedCourses = recommendedCourses,
                        allMentors = mentors,
                        recommendedMentors = recommendedMentors,
                        boostedCourses = boostedCourses,
                        isOffline = false
                    )
                }
                // Apply filter after data is loaded
                applyFilter(_uiState.value.selectedCategory)

            } catch (e: Exception) {
                // Log to analytics/crash reporting
                Log.e("HomeViewModel", "Error fetching home data", e)

                val errorMessage = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    else -> e.localizedMessage ?: "Failed to fetch home data"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage,
                        isOffline = e is IOException // Set offline flag if network error
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

    fun onCategorySelected(category: AppCategory) {
        applyFilter(category)
    }

    fun applyFilter(category: AppCategory) {
        _uiState.update { state ->
            val filteredCourses = if (category == AppCategory.OTHER) {
                state.allCourses
            } else {
                state.allCourses.filter { it.category == category }
            }

            val filteredMentors = if (category == AppCategory.OTHER) {
                state.allMentors
            } else {
                state.allMentors.filter { mentor ->
                    mentor.specialization == category ||
                            mentor.specialization.specializations.any { specRes ->
                                application.getString(specRes).equals(
                                    category.name.replace("_", " "),
                                    ignoreCase = true
                                )
                            }
                }
            }

            state.copy(
                selectedCategory = category,
                courses = filteredCourses,
                mentors = filteredMentors
            )
        }
    }

    fun toggleSaveCourse(courseId: String) {
        val user = authRepository.getCachedUser()
        val userId = user?.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(savingCourseIds = it.savingCourseIds + courseId) }
            val response = authRepository.toggleSaveCourse(userId, courseId)
            if (response is Response.Success) {
                // Refresh data to update UI without global loading
                // Use forceRefresh = false to avoid shuffling the course list from the repository
                fetchHomeData(forceRefresh = false, showLoading = false)
            }
            _uiState.update { it.copy(savingCourseIds = it.savingCourseIds - courseId) }
        }
    }
}


