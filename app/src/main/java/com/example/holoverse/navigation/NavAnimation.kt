package com.example.holoverse.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

object NavAnimations {
    private const val DURATION = 450
    private val EASING = FastOutSlowInEasing

    fun forward(): ContentTransform {
        return (slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(DURATION, easing = EASING)
        ) + fadeIn(animationSpec = tween(DURATION))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(DURATION, easing = EASING)
                ) + fadeOut(animationSpec = tween(DURATION)))
    }

    fun backward(): ContentTransform {
        return (slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(DURATION, easing = EASING)
        ) + fadeIn(animationSpec = tween(DURATION))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(DURATION, easing = EASING)
                ) + fadeOut(animationSpec = tween(DURATION)))
    }
}