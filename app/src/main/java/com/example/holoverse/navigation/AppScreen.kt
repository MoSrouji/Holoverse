package com.example.holoverse.navigation

import kotlinx.serialization.Serializable

sealed interface AppDestination {
    @Serializable
    data object AuthGraph : AppDestination

    @Serializable
    data object HomeGraph : AppDestination

    @Serializable
    data object HoloIntro : AppDestination

    @Serializable
    data object Login : AppDestination

    @Serializable
    data object SignUp : AppDestination

    @Serializable
    data object SignUpTeacherProfile : AppDestination

    @Serializable
    data object SignUpTeacherProfessional : AppDestination

    @Serializable
    data object HomeScreen : AppDestination

    @Serializable
    data object Profile : AppDestination

    @Serializable
    data object Category : AppDestination

    @Serializable
    data object Mentor : AppDestination

    @Serializable
    data object PopularCourses : AppDestination

    @Serializable
    data object Search : AppDestination

    @Serializable
    data object CreateCourse : AppDestination
    @Serializable
    data object Transactions : AppDestination
}
