package com.example.holoverse.reviews.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    onRatingChanged: (Float) -> Unit = {},
    stars: Int = 5,
    starsColor: Color = Color(0xFFFFC107),
    starSize: Dp = 24.dp,
    isEditable: Boolean = false
) {
    Row(modifier = modifier) {
        for (i in 1..stars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = starsColor,
                modifier = Modifier
                    .size(starSize)
                    .clickable(enabled = isEditable) {
                        onRatingChanged(i.toFloat())
                    }
            )
        }
    }
}

