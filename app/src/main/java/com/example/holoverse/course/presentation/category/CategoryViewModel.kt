package com.example.holoverse.course.presentation.category

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.core.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CourseRepo,
    private val authRepository: AuthRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _coursesState = mutableStateOf<Response<List<Courses>>>(Response.Loading)
    val coursesState: State<Response<List<Courses>>> = _coursesState

    private val _category = mutableStateOf(AppCategory.OTHER)
    val category: State<AppCategory> = _category

    private val _savedCourseIds = mutableStateOf<List<String>>(emptyList())
    val savedCourseIds: State<List<String>> = _savedCourseIds

    private val _savingCourseIds = mutableStateOf<Set<String>>(emptySet())
    val savingCourseIds: State<Set<String>> = _savingCourseIds

    fun initialize(category: AppCategory) {
        if (_category.value == category) return
        _category.value = category
        getCoursesByCategory(category)
        loadSavedCourses()
    }

    private fun loadSavedCourses() {
        val currentUser = authRepository.getCachedUser()
        _savedCourseIds.value = when (currentUser) {
            is User.Student -> currentUser.savedCourses ?: emptyList()
            is User.Mentor -> currentUser.savedCourses ?: emptyList()
            else -> emptyList()
        }
    }

    private fun getCoursesByCategory(category: AppCategory) {
        viewModelScope.launch {
            repository.getCoursesByCategory(category).collectLatest { response ->
                if (response is Response.Success) {
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
                    _coursesState.value = Response.Success(courses)
                } else {
                    _coursesState.value = response
                }
            }
        }
    }

    fun toggleSaveCourse(courseId: String) {
        val userId = authRepository.getCachedUser()?.userId ?: return

        viewModelScope.launch {
            _savingCourseIds.value = _savingCourseIds.value + courseId

            val response = authRepository.toggleSaveCourse(userId, courseId)

            if (response is Response.Success) {
                val isSaved = _savedCourseIds.value.contains(courseId)
                if (isSaved) {
                    _savedCourseIds.value = _savedCourseIds.value - courseId
                } else {
                    _savedCourseIds.value = _savedCourseIds.value + courseId
                }
            }
            _savingCourseIds.value = _savingCourseIds.value - courseId
        }
    }
}


