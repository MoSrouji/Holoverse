package com.example.holoverse.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface NavigationIntent {
    data class NavigateTo(
        val route: Any,
        val builder: NavOptionsBuilder.() -> Unit = {}
    ) : NavigationIntent

    data class NavigateAndPopUpTo(
        val route: Any,
        val popUpToRoute: Any,
        val inclusive: Boolean = false
    ) : NavigationIntent

    data object NavigateBack : NavigationIntent
}

@Singleton
class AppNavigator @Inject constructor() {

    private val _navigationIntents = Channel<NavigationIntent>(Channel.CONFLATED)
    val navigationIntents = _navigationIntents.receiveAsFlow()

    fun navigateTo(
        destination: Any,
        builder: NavOptionsBuilder.() -> Unit = {}
    ) {
        _navigationIntents.trySend(NavigationIntent.NavigateTo(destination, builder))
    }

    fun navigateAndPopUpTo(
        destination: Any,
        popUpTo: Any,
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
