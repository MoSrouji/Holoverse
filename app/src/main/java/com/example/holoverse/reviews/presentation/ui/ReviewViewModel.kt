package com.example.holoverse.reviews.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.reviews.domain.Review
import com.example.holoverse.reviews.domain.ReviewRepository
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import com.example.holoverse.core.utils.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.holoverse.auth.domain.entities.User as AuthUser

data class ReviewUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val averageRating: Double = 0.0,
    val isSubmitting: Boolean = false,
    val currentUser: AuthUser? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository,
    private val preferenceManager: PreferenceManager,
    private val translationManager: TranslationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(currentUser = preferenceManager.getUser())
    }

    fun loadReviews(targetId: String) {
        viewModelScope.launch {
            repository.getReviewsByTargetId(targetId).collect { response ->
                when (response) {
                    is Response.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Response.Success -> {
                        val targetLang = preferenceManager.getLanguage() ?: "en"
                        val reviews = if (targetLang != "en") {
                            response.data.map { review ->
                                review.copy(
                                    comment = translationManager.translate(review.comment, targetLang = targetLang)
                                )
                            }
                        } else {
                            response.data
                        }
                        val average = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reviews = reviews,
                            averageRating = average,
                            error = null
                        )
                    }
                    is Response.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun submitReview(review: Review) {
        viewModelScope.launch {
            repository.addReview(review).collect { response ->
                when (response) {
                    is Response.Loading -> _uiState.value = _uiState.value.copy(isSubmitting = true)
                    is Response.Success -> {
                        _uiState.value = _uiState.value.copy(isSubmitting = false)
                        // Refresh reviews
                        loadReviews(review.targetId)
                    }
                    is Response.Error -> _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = response.message
                    )
                }
            }
        }
    }
}


