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
import com.example.holoverse.home.domain.use_case.GetBoostedCoursesUseCase
import com.example.holoverse.home.domain.use_case.GetHomeContentUseCase
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
    val isPaginatingCourses: Boolean = false,
    val isPaginatingMentors: Boolean = false,
    val currentUser: User? = null,
    val courses: List<Courses> = emptyList(),
    val allCourses: List<Courses> = emptyList(),
    val enrolledCourses: List<Courses> = emptyList(),
    val savedCourses: List<Courses> = emptyList(),
    val categories: List<AppCategory> = emptyList(),
    val recommendedCourses: List<Courses> = emptyList(),
    val mentors: List<User.Mentor> = emptyList(),
    val allMentors: List<User.Mentor> = emptyList(),
    val recommendedMentors: List<User.Mentor> = emptyList(),
    val boostedCourses: List<BoostedCourse> = emptyList(),
    val lastCourseDocument: DocumentSnapshot? = null,
    val lastMentorDocument: DocumentSnapshot? = null,
    val savingCourseIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val courseSearchResults: List<Courses> = emptyList(),
    val mentorSearchResults: List<User.Mentor> = emptyList(),
    val isSearching: Boolean = false,
    val selectedTab: HomeTab = HomeTab.Explore,
    val selectedCategory: AppCategory = AppCategory.OTHER,
    val error: String? = null,
    val isOffline: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val getBoostedCoursesUseCase: GetBoostedCoursesUseCase,
    private val fetchDataRepository: FetchDataRepository,
    private val searchRepository: SearchRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    private val preferenceManager: PreferenceManager,
    private val translationManager: TranslationManager,
    private val application: Application
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(HomeUiState(currentUser = authRepository.getCachedUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

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
                    if (currentUser != null && user != null) {
                        fetchHomeData(forceRefresh = false)
                    }
                } else if (user != currentUser) {
                    _uiState.update { it.copy(currentUser = user) }
                }
            }
        }
    }

    private suspend fun processCourses(rawCourses: List<Courses>, userInterests: List<String>): List<Courses> {
        val targetLang = preferenceManager.getLanguage() ?: "en"
        return withContext(Dispatchers.Default) {
            rawCourses.map { course ->
                if (targetLang != "en") {
                    course.copy(
                        name = translationManager.translate(course.name, targetLang = targetLang),
                        description = translationManager.translate(course.description, targetLang = targetLang),
                        instructorName = translationManager.translate(course.instructorName, targetLang = targetLang)
                    )
                } else {
                    course
                }
            }
        }
    }

    private suspend fun processMentors(rawMentors: List<User.Mentor>, userInterests: List<String>): List<User.Mentor> {
        val targetLang = preferenceManager.getLanguage() ?: "en"
        return withContext(Dispatchers.Default) {
            rawMentors.map { mentor ->
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
        }
    }

    private fun fetchHomeData(forceRefresh: Boolean = false, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val homeContent = getHomeContentUseCase(forceRefresh)
                val boostedCourses = getBoostedCoursesUseCase()

                val userInterests = when (val user = homeContent.currentUser) {
                    is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
                    is User.Mentor -> user.subjects ?: emptyList()
                    else -> emptyList()
                }.distinct().map { it.trim().replace(" ", "_").uppercase() }

                val processedCourses = processCourses(homeContent.popularCourses, userInterests)
                val processedMentors = processMentors(homeContent.popularMentors, userInterests)
                
                val recommendedCourses = processCourses(homeContent.recommendedCourses, userInterests)
                val recommendedMentors = processMentors(homeContent.recommendedMentors, userInterests)

                val allProcessedCourses = (processedCourses + recommendedCourses).distinctBy { it.id }
                val allProcessedMentors = (processedMentors + recommendedMentors).distinctBy { it.userId }

                val currentCategory = _uiState.value.selectedCategory
                val filteredCourses = if (currentCategory == AppCategory.OTHER) {
                    allProcessedCourses
                } else {
                    allProcessedCourses.filter { it.category == currentCategory }
                }

                val filteredMentors = if (currentCategory == AppCategory.OTHER) {
                    allProcessedMentors
                } else {
                    allProcessedMentors.filter { it.specialization == currentCategory }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = homeContent.currentUser,
                        courses = filteredCourses,
                        allCourses = allProcessedCourses,
                        enrolledCourses = processCourses(homeContent.enrolledCourses, userInterests),
                        savedCourses = processCourses(homeContent.savedCourses, userInterests),
                        categories = homeContent.categories,
                        recommendedCourses = recommendedCourses,
                        mentors = filteredMentors,
                        allMentors = allProcessedMentors,
                        recommendedMentors = recommendedMentors,
                        boostedCourses = boostedCourses,
                        lastCourseDocument = homeContent.lastCourseDocument,
                        lastMentorDocument = homeContent.lastMentorDocument,
                        isOffline = false
                    )
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching home data", e)
                val errorMessage = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    else -> e.localizedMessage ?: "Failed to fetch home data"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage, isOffline = e is IOException) }
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

    fun loadMoreCourses() {
        val state = _uiState.value
        val lastDoc = state.lastCourseDocument ?: return
        if (state.isLoading || state.isPaginatingCourses) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginatingCourses = true) }
            try {
                val (rawCourses, nextDoc) = fetchDataRepository.fetchCourses(
                    forceRefresh = true,
                    limit = 20,
                    lastVisible = lastDoc
                )
                
                val user = state.currentUser
                val userInterests = when (user) {
                    is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
                    is User.Mentor -> user.subjects ?: emptyList()
                    else -> emptyList()
                }.distinct().map { it.trim().replace(" ", "_").uppercase() }

                val processedCourses = processCourses(rawCourses, userInterests)
                
                _uiState.update { currentState ->
                    val updatedAllCourses = (currentState.allCourses + processedCourses).distinctBy { it.id }
                    
                    currentState.copy(
                        isPaginatingCourses = false,
                        allCourses = updatedAllCourses,
                        lastCourseDocument = nextDoc
                    )
                }
                applyFilter(_uiState.value.selectedCategory)
            } catch (e: Exception) {
                _uiState.update { it.copy(isPaginatingCourses = false, error = e.message) }
            }
        }
    }

    fun loadMoreMentors() {
        val state = _uiState.value
        val lastDoc = state.lastMentorDocument ?: return
        if (state.isLoading || state.isPaginatingMentors) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginatingMentors = true) }
            try {
                val (rawMentors, nextDoc) = fetchDataRepository.fetchMentors(
                    forceRefresh = true,
                    limit = 20,
                    lastVisible = lastDoc
                )
                
                val user = state.currentUser
                val userInterests = when (user) {
                    is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
                    is User.Mentor -> user.subjects ?: emptyList()
                    else -> emptyList()
                }.distinct().map { it.trim().replace("_", " ").uppercase() }

                val processedMentors = processMentors(rawMentors, userInterests)

                _uiState.update { currentState ->
                    val updatedAllMentors = (currentState.allMentors + processedMentors).distinctBy { it.userId }
                    
                    currentState.copy(
                        isPaginatingMentors = false,
                        allMentors = updatedAllMentors,
                        lastMentorDocument = nextDoc
                    )
                }
                applyFilter(_uiState.value.selectedCategory)
            } catch (e: Exception) {
                _uiState.update { it.copy(isPaginatingMentors = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(courseSearchResults = emptyList(), mentorSearchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isSearching = true) }
            
            val user = _uiState.value.currentUser
            val userInterests = when (user) {
                is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
                is User.Mentor -> user.subjects ?: emptyList()
                else -> emptyList()
            }.distinct().map { it.trim().replace(" ", "_").uppercase() }

            // Perform both searches in parallel
            val coursesJob = launch {
                searchRepository.searchCourses(CourseFilters(query = query)).collect { response ->
                    if (response is Response.Success) {
                        val processed = processCourses(response.data, userInterests)
                        _uiState.update { it.copy(courseSearchResults = processed) }
                    }
                }
            }
            
            val mentorsJob = launch {
                searchRepository.searchMentors(MentorFilters(query = query)).collect { response ->
                    if (response is Response.Success) {
                        val processed = processMentors(response.data, userInterests)
                        _uiState.update { it.copy(mentorSearchResults = processed) }
                    }
                }
            }
            
            coursesJob.join()
            mentorsJob.join()
            _uiState.update { it.copy(isSearching = false) }
        }
    }
}


