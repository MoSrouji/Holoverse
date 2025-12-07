package com.example.holoverse.navigation

import androidx.navigation.NavController
import javax.inject.Inject

class AppNavigator @Inject constructor() {

    lateinit var navController: NavController

    fun init(navController: NavController) {
        this.navController = navController
    }

    fun navigateTo(
        screen: AppScreen,
        popUpTo: AppScreen? = null,
        inclusive: Boolean = false
        ) {
        navController.navigate(screen.route){
            popUpTo?.let {
                popUpTo(it.route) {
                    this.inclusive = inclusive
                }
            }
        }
    }
    fun popBackStack(){
        navController.popBackStack()
    }


}