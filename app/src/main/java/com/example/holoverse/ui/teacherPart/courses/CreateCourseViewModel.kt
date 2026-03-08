package com.example.holoverse.ui.teacherPart.courses

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.courses.data.CourseRepo
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val courseRepo: CourseRepo,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _createCourseState = mutableStateOf<Response<Boolean>?>(null)
    val createCourseState: State<Response<Boolean>?> = _createCourseState

    fun createCourse(
        name: String,
        category: String,
        price: String,
        duration: String,
        level: String,
        description: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val currentUser = authRepo.getCurrentUser()
            val instructorId = currentUser?.userId ?: ""
            val instructorName = currentUser?.fullName ?: ""

            val course = Courses(
                id = UUID.randomUUID().toString(),
                name = name,
                category = category,
                price = price.toDoubleOrNull() ?: 0.0,
                duration = duration,
                level = level,
                instructorId = instructorId,
                instructorName = instructorName,
                description = description,
                imageUrl = imageUrl
            )

            courseRepo.addCourse(course).collectLatest { response ->
                _createCourseState.value = response
            }
        }
    }
}
