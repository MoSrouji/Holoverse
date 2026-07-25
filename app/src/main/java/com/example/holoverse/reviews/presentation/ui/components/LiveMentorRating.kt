package com.example.holoverse.reviews.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.R
import com.example.holoverse.reviews.presentation.ui.ReviewViewModel
import java.util.Locale

@Composable
fun LiveMentorRating(
    mentorId: String,
    initialRating: Double,
    initialReviewsCount: Int,
    textStyle: TextStyle,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    showReviewsCount: Boolean = false,
    reviewsCountStyle: TextStyle = MaterialTheme.typography.bodySmall,
    iconTint: Color = Color(0xFFFFB74D),
    viewModel: ReviewViewModel = hiltViewModel(key = mentorId)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mentorId) {
        viewModel.loadReviews(mentorId)
    }

    val rating = if (!uiState.isLoading && uiState.reviews.isNotEmpty()) {
        uiState.averageRating
    } else {
        initialRating
    }

    val reviewsCount = if (!uiState.isLoading && uiState.reviews.isNotEmpty()) {
        uiState.reviews.size
    } else {
        initialReviewsCount
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = stringResource(R.string.rating_desc),
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = textStyle,
            fontWeight = FontWeight.Bold
        )
        if (showReviewsCount) {
            Text(
                text = stringResource(R.string.reviews_count, reviewsCount),
                style = reviewsCountStyle,
                color = Color.Gray
            )
        }
    }
}

