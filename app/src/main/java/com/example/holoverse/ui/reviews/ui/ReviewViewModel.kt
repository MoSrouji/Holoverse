package com.example.holoverse.ui.reviews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.reviews.domain.Review
import com.example.holoverse.reviews.domain.ReviewRepository
import com.example.holoverse.utils.PreferenceManager
import com.example.holoverse.utils.Response
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
    private val preferenceManager: PreferenceManager
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
                    is Response.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reviews = response.data,
                        error = null
                    )
                    is Response.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
        
        viewModelScope.launch {
            repository.getAverageRating(targetId).collect { response ->
                if (response is Response.Success) {
                    _uiState.value = _uiState.value.copy(averageRating = response.data)
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
