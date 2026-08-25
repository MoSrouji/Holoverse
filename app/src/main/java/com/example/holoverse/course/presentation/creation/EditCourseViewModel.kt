package com.example.holoverse.course.presentation.creation

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.cloudinaryservices.domain.use_case.UploadPhotoUseCase
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.CourseSession
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.Quiz
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val courseRepo: CourseRepo,
    private val authRepo: AuthRepository,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
) : ViewModel() {

    private val _editCourseState = mutableStateOf<Response<Boolean>?>(null)
    val editCourseState: State<Response<Boolean>?> = _editCourseState

    private val _courseLoadState = mutableStateOf<Response<Courses?>>(Response.Loading)
    val courseLoadState: State<Response<Courses?>> = _courseLoadState

    private val _uploadImageState = mutableStateOf<Response<String>?>(null)
    val uploadImageState: State<Response<String>?> = _uploadImageState

    private val _mentorCategory = mutableStateOf<AppCategory>(AppCategory.OTHER)
    val mentorCategory: State<AppCategory> = _mentorCategory

    private val _sessions = mutableStateListOf<CourseSession>()
    val sessions: List<CourseSession> = _sessions

    private val _quizzes = mutableStateListOf<Quiz>()
    val quizzes: List<Quiz> = _quizzes

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            courseRepo.getCourseById(courseId).collectLatest { response ->
                _courseLoadState.value = response
                if (response is Response.Success) {
                    val course = response.data
                    if (course != null) {
                        _sessions.clear()
                        _sessions.addAll(course.sessions)
                        _quizzes.clear()
                        _quizzes.addAll(course.quizzes)
                        fetchMentorCategory()
                    }
                }
            }
        }
    }

    private fun fetchMentorCategory() {
        viewModelScope.launch {
            val user = authRepo.getCurrentUser()
            if (user is User.Mentor) {
                _mentorCategory.value = user.specialization
            }
        }
    }

    fun addSession(session: CourseSession) {
        _sessions.add(session)
    }

    fun removeSession(index: Int) {
        if (index in _sessions.indices) {
            _sessions.removeAt(index)
        }
    }

    fun updateSession(index: Int, session: CourseSession) {
        if (index in _sessions.indices) {
            _sessions[index] = session
        }
    }

    fun addQuiz(quiz: Quiz) {
        _quizzes.add(quiz)
    }

    fun removeQuiz(index: Int) {
        if (index in _quizzes.indices) {
            _quizzes.removeAt(index)
        }
    }

    fun updateQuiz(index: Int, quiz: Quiz) {
        if (index in _quizzes.indices) {
            _quizzes[index] = quiz
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uploadImageState.value = Response.Loading
            val result = uploadPhotoUseCase(uri)
            result.onSuccess { url ->
                _uploadImageState.value = Response.Success(url)
            }.onFailure { e ->
                _uploadImageState.value = Response.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCourse(
        courseId: String,
        name: String,
        specialization: String,
        price: String,
        duration: String,
        level: String,
        language: String,
        description: String,
        imageUrl: String,
        availableTimeSlots: List<String>
    ) {
        viewModelScope.launch {
            _editCourseState.value = Response.Loading
            val currentUser = authRepo.getCurrentUser()
            val instructorId = currentUser?.userId ?: ""
            val instructorName = currentUser?.fullName ?: ""

            val originalCourse = (courseLoadState.value as? Response.Success)?.data

            val course = Courses(
                id = courseId,
                name = name,
                category = _mentorCategory.value,
                specialization = specialization,
                price = price.toDoubleOrNull() ?: 0.0,
                duration = duration,
                level = level,
                language = language,
                instructorId = instructorId,
                instructorName = instructorName,
                description = description,
                imageUrl = imageUrl,
                availableTimeSlots = availableTimeSlots,
                sessions = _sessions.toList(),
                quizzes = _quizzes.toList(),
                // Keep analytics data
                numEnrolled = originalCourse?.numEnrolled ?: 0,
                rating = originalCourse?.rating ?: 0.0,
                completionRate = originalCourse?.completionRate ?: 0.0,
                averageProgress = originalCourse?.averageProgress ?: 0.0
            )

            courseRepo.updateCourse(course).collectLatest { response ->
                _editCourseState.value = response
            }
        }
    }

    suspend fun uploadQuizImage(uri: Uri): String? {
        val result = uploadPhotoUseCase(uri)
        return result.getOrNull()
    }
}
