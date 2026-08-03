package com.example.holoverse.course.presentation.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.QuizResult
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.core.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val repository: CourseRepo,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val fetchDataRepository: FetchDataRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _courseState = mutableStateOf<Response<Courses?>>(Response.Loading)
    val courseState: State<Response<Courses?>> = _courseState

    private val _instructorState = mutableStateOf<Response<User.Mentor?>>(Response.Loading)
    val instructorState: State<Response<User.Mentor?>> = _instructorState

    private val _enrollmentState = mutableStateOf<Response<Boolean>?>(null)
    val enrollmentState: State<Response<Boolean>?> = _enrollmentState

    private val _isEnrolled = mutableStateOf(false)
    val isEnrolled: State<Boolean> = _isEnrolled

    private val _isSaved = mutableStateOf(false)
    val isSaved: State<Boolean> = _isSaved

    private val _saveStatus = mutableStateOf<Response<Boolean>?>(null)
    val saveStatus: State<Response<Boolean>?> = _saveStatus

    private val _quizResults = mutableStateOf<Response<List<QuizResult>>>(Response.Loading)
    val quizResults: State<Response<List<QuizResult>>> = _quizResults

    private var currentCourseId: String? = null

    init {
    }

    fun initialize(courseId: String) {
        if (currentCourseId == courseId) return
        currentCourseId = courseId
        getCourseById(courseId)
        checkEnrollmentStatus(courseId)
        checkSavedStatus(courseId)
        fetchQuizResults(courseId)
    }

    private fun fetchQuizResults(courseId: String) {
        val userId = authRepository.getCachedUser()?.userId ?: return
        viewModelScope.launch {
            repository.getQuizResults(userId, courseId).collectLatest { response ->
                _quizResults.value = response
            }
        }
    }

    private fun checkSavedStatus(courseId: String) {
        val user = authRepository.getCachedUser()
        _isSaved.value = when (user) {
            is User.Student -> user.savedCourses?.contains(courseId) == true
            is User.Mentor -> user.savedCourses?.contains(courseId) == true
            else -> false
        }
    }

    private fun checkEnrollmentStatus(courseId: String) {
        val user = authRepository.getCachedUser()
        _isEnrolled.value = when (user) {
            is User.Student -> user.enrolledCourses?.contains(courseId) == true
            is User.Mentor -> user.enrolledCourses?.contains(courseId) == true
            else -> false
        }
    }

    private fun getCourseById(id: String) {
        viewModelScope.launch {
            repository.getCourseById(id).collectLatest { response ->
                if (response is Response.Success) {
                    val course = response.data
                    val targetLang = preferenceManager.getLanguage() ?: "en"

                    if (course != null && targetLang != "en") {
                        val translatedCourse = course.copy(
                            name = translationManager.translate(course.name, targetLang = targetLang),
                            description = translationManager.translate(course.description, targetLang = targetLang),
                            instructorName = translationManager.translate(course.instructorName, targetLang = targetLang),
                            sessions = course.sessions.map { session ->
                                session.copy(
                                    title = translationManager.translate(session.title, targetLang = targetLang),
                                    description = translationManager.translate(session.description, targetLang = targetLang)
                                )
                            }
                        )
                        _courseState.value = Response.Success(translatedCourse)
                    } else {
                        _courseState.value = response
                    }

                    response.data?.instructorId?.let { instructorId ->
                        getInstructorById(instructorId)
                    }
                } else {
                    _courseState.value = response
                }
            }
        }
    }

    private fun getInstructorById(id: String) {
        viewModelScope.launch {
            _instructorState.value = Response.Loading
            try {
                val mentor = fetchDataRepository.fetchMentorById(id)
                val targetLang = preferenceManager.getLanguage() ?: "en"

                if (mentor != null && targetLang != "en") {
                    val translatedMentor = mentor.copy(
                        bio = mentor.bio?.let { translationManager.translate(it, targetLang = targetLang) },
                        certifications = mentor.certifications?.let { translationManager.translate(it, targetLang = targetLang) }
                    )
                    _instructorState.value = Response.Success(translatedMentor)
                } else {
                    _instructorState.value = Response.Success(mentor)
                }
            } catch (e: Exception) {
                _instructorState.value = Response.Error(e.message ?: "Failed to fetch instructor")
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        val user = authRepository.getCachedUser()
        val userId = user?.userId
        
        if (userId == null) {
            _enrollmentState.value = Response.Error("User not logged in")
            return
        }

        val course = (courseState.value as? Response.Success)?.data
        if (course != null && course.instructorId == userId) {
            _enrollmentState.value = Response.Error("Instructors cannot enroll in their own courses")
            return
        }

        viewModelScope.launch {
            authRepository.enrollInCourse(userId, courseId, course?.instructorId ?: "").let { response ->
                _enrollmentState.value = response
                if (response is Response.Success) {
                    _isEnrolled.value = true
                    
                    // Add user to course group chat
                    val course = (courseState.value as? Response.Success)?.data
                    if (course != null) {
                        val profileImageUrl = when (user) {
                            is User.Student -> user.profileImageUrl
                            is User.Mentor -> user.profileImageUrl
                            is User.Admin -> null
                        }
                        chatRepository.createOrJoinGroupChat(
                            courseId = course.id,
                            courseName = course.name,
                            courseImageUrl = course.imageUrl,
                            participantId = userId,
                            participantName = user.fullName ?: "Student",
                            participantImageUrl = profileImageUrl
                        )
                    }
                }
            }
        }
    }
    
    fun resetEnrollmentState() {
        _enrollmentState.value = null
    }

    fun toggleSaveCourse(courseId: String) {
        val user = authRepository.getCachedUser()
        val userId = user?.userId

        if (userId == null) {
            _saveStatus.value = Response.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _saveStatus.value = Response.Loading
            val response = authRepository.toggleSaveCourse(userId, courseId)
            _saveStatus.value = response
            if (response is Response.Success) {
                _isSaved.value = !_isSaved.value
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}


