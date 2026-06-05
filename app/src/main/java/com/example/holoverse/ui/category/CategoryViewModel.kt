package com.example.holoverse.ui.category

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.data.CourseRepo
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CourseRepo
) : ViewModel() {

    private val _coursesState = mutableStateOf<Response<List<Courses>>>(Response.Loading)
    val coursesState: State<Response<List<Courses>>> = _coursesState

    private val _category = mutableStateOf(AppCategory.OTHER)
    val category: State<AppCategory> = _category

    init {
    }

    fun initialize(category: AppCategory) {
        if (_category.value == category) return
        _category.value = category
        getCoursesByCategory(category)
    }

    private fun getCoursesByCategory(category: AppCategory) {
        viewModelScope.launch {
            repository.getCoursesByCategory(category).collectLatest { response ->
                _coursesState.value = response
            }
        }
    }
}
