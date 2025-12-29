package com.example.holoverse.navigation

sealed class AppScreen(val route: String) {
    data object Auth : AppScreen("auth_graph")
    data object Login : AppScreen("login_screen")
    data object SignUp : AppScreen("signup_screen")
    data object SignUpTeacherProfile : AppScreen("signup_screen_teacher_profile")
    data object SignUpTeacherProfessional : AppScreen("signup_screen_teacher_Professional")
    data object HoloIntro : AppScreen("holo_intro")


    data object Home : AppScreen("home_graph")
    data object HomeScreen: AppScreen("home_screen")


}