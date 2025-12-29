package com.example.holoverse.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.holoverse.auth.domain.entities.Teacher
import com.example.holoverse.ui.auth.presentaiton.authentication.signin.SignInScreen
import com.example.holoverse.ui.auth.presentaiton.authentication.signup.SignUpScreen
import com.example.holoverse.ui.collectUserData.teacher.screens.TeacherProfessionalInfoInput
import com.example.holoverse.ui.collectUserData.teacher.screens.TeacherProfileInput
import com.example.holoverse.ui.home.HomeScreen
import com.example.holoverse.ui.spatialTheme.HoloIntroScreen
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: AppNavigator
) {


    navigator.init(navController)

    val sharedState = MutableStateFlow(Teacher())

    SpatialBackground()
    NavHost(
        navController,
        AppScreen.Auth.route
    ) {
        authGraph(navigator, teacherState = sharedState)
    }
    NavHost(
        navController,
        AppScreen.HomeScreen.route
    ) {

    }

}

private fun NavGraphBuilder.authGraph(
    navigator: AppNavigator,
    teacherState: MutableStateFlow<Teacher>
) {

    navigation(
        startDestination = AppScreen.HoloIntro.route,
        route = AppScreen.Auth.route
    ) {


        composable(
            AppScreen.HoloIntro.route,
            enterTransition = { NavAnimations.slideInFromRight() },
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {


            HoloIntroScreen(
                onNavigationComplete = {
                    navigator.navigateTo(AppScreen.Login)
                }
            )

        }
        composable(
            AppScreen.Login.route,
            enterTransition = { NavAnimations.slideOutToUp() },
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            // SpatialBackground()
            // Your LoginScreen composable
            SignInScreen(
                viewModel = hiltViewModel(), // Hilt provides the ViewModel
                navController = navigator,
                navToHomeScreen = {}
            )
        }
        composable(
            AppScreen.SignUp.route,
            enterTransition = { NavAnimations.slideOutToUp() },
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            // Your SignUpScreen composable
            //  SpatialBackground()

            SignUpScreen(
                viewModel = hiltViewModel(),
                navController = navigator,
                navToHomeScreen = {

                }
            )

        }
        // Add other auth screens like ForgotPassword here
    }

    composable(
        AppScreen.SignUpTeacherProfile.route,
        enterTransition = { NavAnimations.slideOutToUp() },
        exitTransition = { NavAnimations.slideOutToDown() }
    ) {
        //  SpatialBackground()
        TeacherProfileInput(
            navController = navigator,
            navToHomeScreen = {},
            viewModel = hiltViewModel(),
            teacherStates = teacherState
        )

    }
    composable(
        AppScreen.SignUpTeacherProfessional.route,
        enterTransition = { NavAnimations.slideOutToUp() },
        exitTransition = { NavAnimations.slideOutToDown() }
    ) {
        // Your SignUpScreen composable
        //  SpatialBackground()

        TeacherProfessionalInfoInput(
            navController = navigator,
            navToHomeScreen = {},
            viewModel = hiltViewModel(),
            teacherStates = teacherState
        )

    }
    // Add other auth screens like ForgotPassword here
}

private fun NavGraphBuilder.appGraph(
    navigator: AppNavigator,
) {

    navigation(
        startDestination = AppScreen.HomeScreen.route,
        route = AppScreen.Home.route
    ) {
        composable(
            AppScreen.SignUpTeacherProfessional.route,
            enterTransition = { NavAnimations.slideOutToUp() },
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {

            HomeScreen()
        }

    }
}
