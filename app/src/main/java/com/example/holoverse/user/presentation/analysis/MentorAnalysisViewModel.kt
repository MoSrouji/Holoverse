package com.example.holoverse.user.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.payment.domain.model.Transaction
import com.example.holoverse.payment.domain.repository.PaymentRepository
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.core.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MentorAnalysisUiState(
    val mentor: User.Mentor? = null,
    val courses: List<Courses> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalStudents: Int = 0,
    val totalFollowers: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageRating: Double = 0.0,
    val averageCompletionRate: Double = 0.0,
    val averageProgress: Double = 0.0,
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class MentorAnalysisViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepo: CourseRepo,
    private val paymentRepository: PaymentRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorAnalysisUiState())
    val uiState: StateFlow<MentorAnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is User.Mentor) {
                val targetLang = preferenceManager.getLanguage() ?: "en"
                val translatedMentor = if (targetLang != "en") {
                    currentUser.copy(
                        fullName = currentUser.fullName?.let { translationManager.translate(it, targetLang = targetLang) },
                        bio = currentUser.bio?.let { translationManager.translate(it, targetLang = targetLang) },
                        certifications = currentUser.certifications?.let { translationManager.translate(it, targetLang = targetLang) }
                    )
                } else {
                    currentUser
                }
                _uiState.update { it.copy(mentor = translatedMentor) }
                fetchCourses(currentUser.userId ?: "")
                fetchTransactions(currentUser.userId ?: "")
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found or not a mentor") }
            }
        }
    }

    private suspend fun fetchCourses(mentorId: String) {
        courseRepo.getCoursesByInstructorId(mentorId).collectLatest { response ->
            when (response) {
                is Response.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Response.Success -> {
                    val rawCourses = response.data ?: emptyList()
                    val targetLang = preferenceManager.getLanguage() ?: "en"
                    val courses = if (targetLang != "en") {
                        rawCourses.map { course ->
                            course.copy(
                                name = translationManager.translate(course.name, targetLang = targetLang),
                                description = translationManager.translate(course.description, targetLang = targetLang),
                                instructorName = translationManager.translate(course.instructorName, targetLang = targetLang)
                            )
                        }
                    } else {
                        rawCourses
                    }
                    calculateStats(courses)
                }
                is Response.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            }
        }
    }

    private suspend fun fetchTransactions(mentorId: String) {
        paymentRepository.getTransactionsForUser(mentorId).collectLatest { response ->
            if (response is Response.Success) {
                val transactions = response.data
                val earnings = transactions.filter { it.receiverId == mentorId }.sumOf { it.amount }
                _uiState.update { it.copy(transactions = transactions, totalRevenue = earnings) }
            }
        }
    }

    private fun calculateStats(courses: List<Courses>) {
        val mentor = _uiState.value.mentor
        val totalStudents = mentor?.totalStudentsTaught ?: courses.sumOf { it.numEnrolled }
        val totalFollowers = mentor?.followersCount ?: mentor?.followers?.size ?: 0
        // totalRevenue is now fetched from transactions in fetchTransactions
        val avgRating = if (courses.isNotEmpty()) courses.sumOf { it.rating } / courses.size else 0.0
        val avgCompletion = if (courses.isNotEmpty()) courses.sumOf { it.completionRate } / courses.size else 0.0
        val avgProgress = if (courses.isNotEmpty()) courses.sumOf { it.averageProgress } / courses.size else 0.0

        _uiState.update { 
            it.copy(
                courses = courses.sortedByDescending { c -> c.numEnrolled },
                isLoading = false,
                totalStudents = totalStudents,
                totalFollowers = totalFollowers,
                averageRating = mentor?.averageRating ?: avgRating,
                averageCompletionRate = avgCompletion,
                averageProgress = avgProgress
            )
        }
    }
}



