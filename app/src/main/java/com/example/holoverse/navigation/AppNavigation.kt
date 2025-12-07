package com.example.holoverse.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.holoverse.ui.auth.presentaiton.authentication.signin.SignInScreen
import com.example.holoverse.ui.auth.presentaiton.authentication.signup.SignUpScreen
import com.example.holoverse.ui.collectUserData.teacher.TeacherProfessionalInfoInput
import com.example.holoverse.ui.collectUserData.teacher.TeacherProfileInput
import com.example.holoverse.ui.spatialTheme.SpatialBackground

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: AppNavigator
) {
    navigator.init(navController)

    SpatialBackground()
    NavHost(
        navController,
        AppScreen.Auth.route
    ) {


        authGraph(navigator)


    }

}

private fun NavGraphBuilder.authGraph(navigator: AppNavigator) {
    navigation(
        startDestination = AppScreen.Login.route,
        route = AppScreen.Auth.route
    ) {
        composable(
            AppScreen.Login.route,
            enterTransition =  { NavAnimations.slideOutToUp() } ,
            exitTransition = { NavAnimations.slideOutToDown()}
        ){
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
            enterTransition = { NavAnimations.slideOutToUp() } ,
            exitTransition = { NavAnimations.slideOutToDown()}
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
            enterTransition = { NavAnimations.slideOutToUp() } ,
            exitTransition = { NavAnimations.slideOutToDown()}
        ) {
            // Your SignUpScreen composable
          //  SpatialBackground()

        TeacherProfileInput(
            navController = navigator,
            navToHomeScreen = {},
            viewModel = hiltViewModel()
        )

        }
    composable(
            AppScreen.SignUpTeacherProfessional.route,
            enterTransition = { NavAnimations.slideOutToUp() } ,
            exitTransition = { NavAnimations.slideOutToDown()}
        ) {
            // Your SignUpScreen composable
          //  SpatialBackground()

        TeacherProfessionalInfoInput(
            navController = navigator,
            navToHomeScreen = {},
            viewModel = hiltViewModel()
        )

        }
        // Add other auth screens like ForgotPassword here
    }
