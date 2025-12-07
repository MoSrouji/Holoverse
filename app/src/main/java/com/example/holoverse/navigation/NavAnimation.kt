package com.example.holoverse.navigation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

object NavAnimations {
    // Default animation specs
    private const val DEFAULT_DURATION = 300
    private val DEFAULT_EASING = FastOutSlowInEasing

    // Slide animations
    fun slideInFromRight() = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

    fun slideOutToLeft() = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )
    fun slideOutToUp() = slideInVertically (
        initialOffsetY = { +it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )
    fun slideOutToDown() = slideOutVertically (
        targetOffsetY = { -it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

}
