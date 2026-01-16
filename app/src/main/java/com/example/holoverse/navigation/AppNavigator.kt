package com.example.holoverse.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNavigator @Inject constructor() {

    private var _navController: NavController? = null
    val navController: NavController
        get() = _navController ?: throw IllegalStateException("NavController not initialized")

    fun init(navController: NavController) {
        this._navController = navController
    }

    fun <T : Any> navigateTo(
        destination: T,
        builder: NavOptionsBuilder.() -> Unit = {}
    ) {
        navController.navigate(destination, builder)
    }

    fun navigateAndPopUpTo(
        destination: Any,
        popUpTo: Any,
        inclusive: Boolean = false
    ) {
        navController.navigate(destination) {
            popUpTo(popUpTo) {
                this.inclusive = inclusive
            }
        }
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}
