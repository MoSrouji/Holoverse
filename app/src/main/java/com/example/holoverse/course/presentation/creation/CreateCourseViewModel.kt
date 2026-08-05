package com.example.holoverse.course.presentation.creation

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.domain.repository.ChatRepository
import com.example.holoverse.cloudinaryservices.domain.use_case.UploadPhotoUseCase
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.CourseSession
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.Quiz
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.payment.domain.model.TransactionType
import com.example.holoverse.payment.domain.repository.PaymentRepository
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val courseRepo: CourseRepo,
    private val authRepo: AuthRepository,
    private val chatRepository: ChatRepository,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val notificationRepo: NotificationRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _createCourseState = mutableStateOf<Response<Boolean>?>(null)
    val createCourseState: State<Response<Boolean>?> = _createCourseState

    private val _boostCourseState = mutableStateOf<Response<Boolean>?>(null)
    val boostCourseState: State<Response<Boolean>?> = _boostCourseState

    private val _lastCreatedCourse = mutableStateOf<Courses?>(null)
    val lastCreatedCourse: State<Courses?> = _lastCreatedCourse

    private val _uploadImageState = mutableStateOf<Response<String>?>(null)
    val uploadImageState: State<Response<String>?> = _uploadImageState

    private val _mentorCategory = mutableStateOf<AppCategory>(AppCategory.OTHER)
    val mentorCategory: State<AppCategory> = _mentorCategory

    private val _sessions = mutableStateListOf<CourseSession>()
    val sessions: List<CourseSession> = _sessions

    private val _quizzes = mutableStateListOf<Quiz>()
    val quizzes: List<Quiz> = _quizzes

    init {
        fetchMentorCategory()
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

    fun createCourse(
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
            _createCourseState.value = Response.Loading
            val currentUser = authRepo.getCurrentUser()
            val instructorId = currentUser?.userId ?: ""
            val instructorName = currentUser?.fullName ?: ""

            val course = Courses(
                id = UUID.randomUUID().toString(),
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
                quizzes = _quizzes.toList()
            )

            courseRepo.addCourse(course).collectLatest { response ->
                if (response is Response.Success) {
                    _lastCreatedCourse.value = course
                    // Link course to mentor
                    authRepo.addCourseToMentor(instructorId, course.id)
                    
                    // Create group chat for the course
                    val profileImageUrl = (currentUser as? User.Mentor)?.profileImageUrl
                    chatRepository.createOrJoinGroupChat(
                        courseId = course.id,
                        courseName = course.name,
                        courseImageUrl = course.imageUrl,
                        participantId = instructorId,
                        participantName = instructorName,
                        participantImageUrl = profileImageUrl,
                        mentorId = instructorId,
                        mentorName = instructorName
                    )

                    // Send notification to followers
                    notificationRepo.sendCourseNotificationToFollowers(
                        mentorId = instructorId,
                        mentorName = instructorName,
                        courseId = course.id,
                        courseName = course.name
                    )
                }
                _createCourseState.value = response
            }
        }
    }

    fun boostCourse(boostedCourse: BoostedCourse, planPrice: Double) {
        viewModelScope.launch {
            _boostCourseState.value = Response.Loading
            val currentUser = authRepo.getCurrentUser()
            val mentorId = currentUser?.userId ?: ""

            // Payment for boosting
            try {
                val adminResponse = authRepo.getSupportAdmin().first { it !is Response.Loading }
                val adminId = if (adminResponse is Response.Success) adminResponse.data.userId ?: "" else ""

                if (adminId.isNotEmpty() && planPrice > 0) {
                    paymentRepository.transferFunds(
                        senderId = mentorId,
                        receiverId = adminId,
                        amount = planPrice,
                        type = TransactionType.COURSE_BOOST,
                        metadata = mapOf("courseId" to boostedCourse.courseId, "planDuration" to boostedCourse.planDuration)
                    )
                }
            } catch (e: Exception) {
                _boostCourseState.value = Response.Error("Payment for boost failed: ${e.message}")
                return@launch
            }

            courseRepo.boostCourse(boostedCourse).collectLatest { response ->
                _boostCourseState.value = response
            }
        }
    }

    suspend fun uploadQuizImage(uri: Uri): String? {
        val result = uploadPhotoUseCase(uri)
        return result.getOrNull()
    }
}


