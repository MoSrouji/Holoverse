package com.example.holoverse.ui.category

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.courses.data.CourseRepo
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CourseRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _coursesState = mutableStateOf<Response<List<Courses>>>(Response.Loading)
    val coursesState: State<Response<List<Courses>>> = _coursesState

    private val _categoryName = mutableStateOf("")
    val categoryName: State<String> = _categoryName

    init {
        savedStateHandle.get<String>("categoryName")?.let { category ->
            _categoryName.value = category
            getCoursesByCategory(category)
        }
    }

    private fun getCoursesByCategory(category: String) {
        viewModelScope.launch {
            repository.getCoursesByCategory(category).collectLatest { response ->
                _coursesState.value = response
            }
        }
    }
}
