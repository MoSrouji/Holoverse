package com.example.holoverse.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface NavigationIntent {
    data class NavigateTo(
        val route: NavKey
    ) : NavigationIntent

    data class NavigateAndPopUpTo(
        val route: NavKey,
        val popUpToRoute: NavKey,
        val inclusive: Boolean = false
    ) : NavigationIntent

    data object NavigateBack : NavigationIntent
}

@Singleton
class AppNavigator @Inject constructor() {

    private val _navigationIntents = Channel<NavigationIntent>(Channel.CONFLATED)
    val navigationIntents = _navigationIntents.receiveAsFlow()

    fun navigateTo(
        destination: NavKey
    ) {
        _navigationIntents.trySend(NavigationIntent.NavigateTo(destination))
    }

    fun navigateAndPopUpTo(
        destination: NavKey,
        popUpTo: NavKey,
        inclusive: Boolean = false
    ) {
        _navigationIntents.trySend(
            NavigationIntent.NavigateAndPopUpTo(
                route = destination,
                popUpToRoute = popUpTo,
                inclusive = inclusive
            )
        )
    }

    fun popBackStack() {
        _navigationIntents.trySend(NavigationIntent.NavigateBack)
    }
}
