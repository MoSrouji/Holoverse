package com.example.holoverse.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searchType: SearchType = SearchType.COURSES,
    val courseFilters: CourseFilters = CourseFilters(),
    val mentorFilters: MentorFilters = MentorFilters(),
    val searchResults: SearchResults = SearchResults(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val recentSearches: List<String> = emptyList()
)

enum class SearchType {
    COURSES, MENTORS
}

data class SearchResults(
    val courses: List<Courses> = emptyList(),
    val mentors: List<User.Mentor> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val authRepository: AuthRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var recentSearchesJob: Job? = null

    init {
        observeRecentSearches()
    }

    private fun observeRecentSearches() {
        recentSearchesJob?.cancel()
        recentSearchesJob = viewModelScope.launch {
            val user = authRepository.getCachedUser() ?: authRepository.getCurrentUser()
            user?.userId?.let { userId ->
                _uiState.collectLatest { state ->
                    searchRepository.getRecentSearches(userId, state.searchType).collect { searches ->
                        _uiState.update { it.copy(recentSearches = searches) }
                    }
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        triggerSearch(withDebounce = true)
    }

    fun onSearchTypeChange(type: SearchType) {
        _uiState.update { it.copy(searchType = type) }
        triggerSearch(withDebounce = false)
    }

    fun updateCourseCategory(category: AppCategory?) {
        _uiState.update { it.copy(courseFilters = it.courseFilters.copy(category = category)) }
        triggerSearch(withDebounce = false)
    }

    fun updateCourseLevel(level: String?) {
        _uiState.update { it.copy(courseFilters = it.courseFilters.copy(level = level)) }
        triggerSearch(withDebounce = false)
    }

    fun updateCoursePriceRange(min: Double?, max: Double?) {
        _uiState.update {
            it.copy(
                courseFilters = it.courseFilters.copy(
                    minPrice = min,
                    maxPrice = max
                )
            )
        }
        triggerSearch(withDebounce = false)
    }

    fun updateMentorSpecialization(specialization: AppCategory?) {
        _uiState.update { it.copy(mentorFilters = it.mentorFilters.copy(specialization = specialization)) }
        triggerSearch(withDebounce = false)
    }

    fun updateMentorHourlyRate(min: Double?, max: Double?) {
        _uiState.update {
            it.copy(
                mentorFilters = it.mentorFilters.copy(
                    minHourlyRate = min,
                    maxHourlyRate = max
                )
            )
        }
        triggerSearch(withDebounce = false)
    }

    fun updateMentorRating(minRating: Double?) {
        _uiState.update { it.copy(mentorFilters = it.mentorFilters.copy(minRating = minRating)) }
        triggerSearch(withDebounce = false)
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                courseFilters = CourseFilters(),
                mentorFilters = MentorFilters()
            )
        }
        triggerSearch(withDebounce = false)
    }

    fun updateCourseFilters(filters: CourseFilters) {
        _uiState.update { it.copy(courseFilters = filters) }
        triggerSearch(withDebounce = false)
    }

    fun updateMentorFilters(filters: MentorFilters) {
        _uiState.update { it.copy(mentorFilters = filters) }
        triggerSearch(withDebounce = false)
    }

    private fun triggerSearch(withDebounce: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (withDebounce) {
                delay(500)
            }
            performSearch()
        }
    }

    private suspend fun performSearch() {
        val currentState = _uiState.value
        
        // Save to recent searches if query is not empty and search was triggered
        if (currentState.query.isNotBlank()) {
            saveSearch(currentState.query, currentState.searchType)
        }

        if (currentState.searchType == SearchType.COURSES) {
            searchRepository.searchCourses(currentState.courseFilters.copy(query = currentState.query))
                .collect { response ->
                    handleCourseResponse(response)
                }
        } else {
            searchRepository.searchMentors(currentState.mentorFilters.copy(query = currentState.query))
                .collect { response ->
                    handleMentorResponse(response)
                }
        }
    }

    private fun saveSearch(query: String, type: SearchType) {
        viewModelScope.launch {
            val user = authRepository.getCachedUser() ?: authRepository.getCurrentUser()
            user?.let {
                searchRepository.saveRecentSearch(it, query, type)
            }
        }
    }

    private fun handleCourseResponse(response: Response<List<Courses>>) {
        when (response) {
            is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
            is Response.Success -> {
                viewModelScope.launch {
                    val targetLang = preferenceManager.getLanguage() ?: "en"
                    val courses = if (targetLang != "en") {
                        response.data.map { course ->
                            course.copy(
                                name = translationManager.translate(course.name, targetLang = targetLang),
                                description = translationManager.translate(course.description, targetLang = targetLang),
                                instructorName = translationManager.translate(course.instructorName, targetLang = targetLang)
                            )
                        }
                    } else {
                        response.data
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = it.searchResults.copy(courses = courses),
                            error = null
                        )
                    }
                }
            }

            is Response.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    private fun handleMentorResponse(response: Response<List<User.Mentor>>) {
        when (response) {
            is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
            is Response.Success -> {
                viewModelScope.launch {
                    val targetLang = preferenceManager.getLanguage() ?: "en"
                    val mentors = if (targetLang != "en") {
                        response.data.map { mentor ->
                            mentor.copy(
                                fullName = mentor.fullName?.let { translationManager.translate(it, targetLang = targetLang) },
                                bio = mentor.bio?.let { translationManager.translate(it, targetLang = targetLang) },
                                certifications = mentor.certifications?.let { translationManager.translate(it, targetLang = targetLang) }
                            )
                        }
                    } else {
                        response.data
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = it.searchResults.copy(mentors = mentors),
                            error = null
                        )
                    }
                }
            }

            is Response.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun removeRecentSearch(item: String) {
        viewModelScope.launch {
            val user = authRepository.getCachedUser() ?: authRepository.getCurrentUser()
            user?.let {
                searchRepository.removeRecentSearch(it, item, _uiState.value.searchType)
            }
        }
    }
}



