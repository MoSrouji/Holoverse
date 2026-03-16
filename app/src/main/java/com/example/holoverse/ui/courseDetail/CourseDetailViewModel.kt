package com.example.holoverse.ui.courseDetail

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
class CourseDetailViewModel @Inject constructor(
    private val repository: CourseRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _courseState = mutableStateOf<Response<Courses?>>(Response.Loading)
    val courseState: State<Response<Courses?>> = _courseState

    init {
        // Assuming we pass courseId via navigation
        savedStateHandle.get<String>("courseId")?.let { courseId ->
            getCourseById(courseId)
        }
    }

    private fun getCourseById(id: String) {
        viewModelScope.launch {
            repository.getCourseById(id).collectLatest { response ->
                _courseState.value = response
            }
        }
    }
}
