package com.example.holoverse.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val recentSearches: List<String> = listOf(
        "3D Design", "Graphic Design", "Programming", "SEO & Marketing",
        "Web Development", "Office Productivity", "Personal Development"
    )
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
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce search
            performSearch()
        }
    }

    fun onSearchTypeChange(type: SearchType) {
        _uiState.update { it.copy(searchType = type) }
        performSearch()
    }

    fun updateCourseFilters(filters: CourseFilters) {
        _uiState.update { it.copy(courseFilters = filters) }
        performSearch()
    }

    fun updateMentorFilters(filters: MentorFilters) {
        _uiState.update { it.copy(mentorFilters = filters) }
        performSearch()
    }

    private fun performSearch() {
        val currentState = _uiState.value
        if (currentState.searchType == SearchType.COURSES) {
            searchCourses(currentState.courseFilters.copy(query = currentState.query))
        } else {
            searchMentors(currentState.mentorFilters.copy(query = currentState.query))
        }
    }

    private fun searchCourses(filters: CourseFilters) {
        viewModelScope.launch {
            searchRepository.searchCourses(filters).collect { response ->
                when (response) {
                    is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Response.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                searchResults = it.searchResults.copy(courses = response.data),
                                error = null
                            )
                        }
                    }
                    is Response.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.massage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun searchMentors(filters: MentorFilters) {
        viewModelScope.launch {
            searchRepository.searchMentors(filters).collect { response ->
                when (response) {
                    is Response.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Response.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                searchResults = it.searchResults.copy(mentors = response.data),
                                error = null
                            )
                        }
                    }
                    is Response.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.massage
                            )
                        }
                    }
                }
            }
        }
    }

    fun removeRecentSearch(item: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filter { it != item })
        }
    }
}