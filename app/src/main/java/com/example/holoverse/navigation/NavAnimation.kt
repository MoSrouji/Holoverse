package com.example.holoverse.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

object NavAnimations {
    private const val DEFAULT_DURATION = 300
    private val DEFAULT_EASING = FastOutSlowInEasing

    fun slideInFromRight() = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

    fun slideOutToLeft() = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

    fun slideInFromLeft() = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

    fun slideOutToRight() = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

    fun slideInFromBottom() = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )

//    fun slideOutToUp(): @JvmSuppressWildcards EnterTransition? = slideOutVertically(
//        targetOffsetY = { -it },
//        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
//    )

    fun slideOutToDown() = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(DEFAULT_DURATION, easing = DEFAULT_EASING)
    )
}
