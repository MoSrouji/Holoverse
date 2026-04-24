package com.example.holoverse.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.category.CategoryCoursesScreen
import com.example.holoverse.ui.category.CategoryScreen
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signin.SignInScreen
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpScreen
import com.example.holoverse.ui.collectUserData.teacher.screens.TeacherProfessionalInfoInput
import com.example.holoverse.ui.collectUserData.teacher.screens.TeacherProfileInput
import com.example.holoverse.ui.commonPart.profile.ProfileScreen
import com.example.holoverse.ui.home.HomeScreen
import com.example.holoverse.utils.HoloBottomDock
import com.example.holoverse.ui.search.SearchScreen
import com.example.holoverse.ui.spatialTheme.HoloIntroScreen
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.home.mentorsList.RecommendedMentorsScreen
import com.example.holoverse.ui.home.mentorsList.TopMentorsScreen
import com.example.holoverse.ui.teacherPart.courses.CreateCourseScreen
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.compose.currentStateAsState
import com.example.holoverse.ui.chat.ChatScreen
import com.example.holoverse.ui.commonPart.profile.EditProfileScreen
import com.example.holoverse.ui.commonPart.profile.TermsAndConditionsScreen
import com.example.holoverse.ui.courseDetail.CourseDetailScreen
import com.example.holoverse.ui.home.coursesList.PopularCoursesScreen
import com.example.holoverse.ui.home.coursesList.RecommendationScreen
import com.example.holoverse.ui.mentor.MentorProfileScreen
import com.example.holoverse.ui.three_D_Part.ar.ArScreen
import com.example.holoverse.ui.three_D_Part.gallery.GalleryScreen
import com.example.holoverse.ui.three_D_Part.viewer.ViewerScreen
import com.example.holoverse.ui.transaction.TransactionScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: AppNavigator,
    isLoggedIn: Boolean = false,
    darkTheme: Boolean
) {
    navigator.init(navController)

    val sharedState = MutableStateFlow(User.Mentor())

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isInHomeGraph =
        currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.HomeGraph>() } == true
    val isChatScreen = currentDestination?.hasRoute<AppDestination.ChatScreen>() == true

    val showBottomBar = isInHomeGraph && !isChatScreen

    SpatialBackground(darkTheme = darkTheme)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                HoloBottomDock(
                    navController = navController,
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) AppDestination.HomeGraph else AppDestination.AuthGraph,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { NavAnimations.slideInFromRight() },
            exitTransition = { NavAnimations.slideOutToLeft() },
            popEnterTransition = { NavAnimations.slideInFromLeft() },
            popExitTransition = { NavAnimations.slideOutToRight() }
        ) {
            authGraph(navigator, mentorState = sharedState)
            homeGraph(navigator, darkTheme)
            subGraph(navigator)
        }
    }
}

private fun NavGraphBuilder.authGraph(
    navigator: AppNavigator,
    mentorState: MutableStateFlow<User.Mentor>
) {
    navigation<AppDestination.AuthGraph>(
        startDestination = AppDestination.HoloIntro
    ) {
        composable<AppDestination.HoloIntro>(
            enterTransition = { NavAnimations.slideInFromRight() },
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            HoloIntroScreen(
                onNavigationComplete = {
                    navigator.navigateTo(AppDestination.Login)
                }
            )
        }

        composable<AppDestination.Login>(
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            SignInScreen(
                viewModel = hiltViewModel(),
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeGraph,
                        popUpTo = AppDestination.AuthGraph,
                        inclusive = true
                    )
                }
            )
        }

        composable<AppDestination.SignUp>(
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            SignUpScreen(
                viewModel = hiltViewModel(),
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeGraph,
                        popUpTo = AppDestination.AuthGraph,
                        inclusive = true
                    )
                }
            )
        }

        composable<AppDestination.SignUpTeacherProfile>(
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            TeacherProfileInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeGraph,
                        popUpTo = AppDestination.AuthGraph,
                        inclusive = true
                    )
                },
                viewModel = hiltViewModel(),
                mentorStates = mentorState
            )
        }

        composable<AppDestination.SignUpTeacherProfessional>(
            exitTransition = { NavAnimations.slideOutToDown() }
        ) {
            TeacherProfessionalInfoInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeGraph,
                        popUpTo = AppDestination.AuthGraph,
                        inclusive = true
                    )
                },
                viewModel = hiltViewModel(),
                mentorStates = mentorState
            )
        }
    }
}

private fun NavGraphBuilder.homeGraph(
    navigator: AppNavigator,
    darkTheme: Boolean
) {
    navigation<AppDestination.HomeGraph>(
        startDestination = AppDestination.HomeScreen
    ) {
        composable<AppDestination.HomeScreen> {
            HomeScreen(
                appNavigator = navigator,
                onCategoryClick = {
                    navigator.navigateTo(destination = AppDestination.Category)
                },
                onPopularCoursesClick = {
                    navigator.navigateTo(destination = AppDestination.PopularCourses)
                },
                onTopMentorClick = {
                    navigator.navigateTo(destination = AppDestination.Mentor())
                },
                onMentorClick = { mentorId ->
                    navigator.navigateTo(destination = AppDestination.MentorProfile(mentorId = mentorId))
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.Profile> {
            ProfileScreen(navController = navigator, darkTheme = darkTheme)
        }

        composable<AppDestination.Category> {
            CategoryScreen(appNavigator = navigator, darkTheme = darkTheme)
        }
        composable<AppDestination.CategoryCourses> {
            CategoryCoursesScreen(appNavigator = navigator, darkTheme = darkTheme)
        }
        composable<AppDestination.GalleryScreen> {
            GalleryScreen(appNavigator = navigator, darkTheme = darkTheme)
        }
        composable<AppDestination.Transactions> {
            TransactionScreen(appNavigator = navigator)
        }
        composable<AppDestination.ChatList> {
            ChatScreen(
                darkTheme = darkTheme,
                onNavigateToConversation = { mentorId ->
                    navigator.navigateTo(AppDestination.ChatScreen(mentorId = mentorId))
                }
            )
        }
        composable<AppDestination.ChatScreen> { backStackEntry ->
            val chatScreen: AppDestination.ChatScreen = backStackEntry.toRoute()
            ChatScreen(
                darkTheme = darkTheme,
                mentorId = chatScreen.mentorId,
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.TermsAndConditions> {
            TermsAndConditionsScreen(navController = navigator)
        }
    }
}

private fun NavGraphBuilder.subGraph(
    navigator: AppNavigator
) {
    navigation<AppDestination.SubGraph>(
        startDestination = AppDestination.CreateCourse
    ) {
        composable<AppDestination.CreateCourse> {
            CreateCourseScreen(
                appNavigator = navigator
            )
        }
        composable<AppDestination.Search> {
            SearchScreen(appNavigator = navigator)
        }
        composable<AppDestination.EditProfile> {
            EditProfileScreen()
        }
        composable<AppDestination.PopularCourses> {
            PopularCoursesScreen(
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.Recommended> {
            RecommendationScreen(
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.RecommendedMentors> {
            RecommendedMentorsScreen(
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.CourseDetail> {
            CourseDetailScreen(
                onBackClick = { navigator.popBackStack() },
                onApplyClick = { /* Handle apply course */ }
            )
        }
        composable<AppDestination.MentorProfile> { backStackEntry ->
            val mentorProfile: AppDestination.MentorProfile = backStackEntry.toRoute()
            MentorProfileScreen(
                mentorId = mentorProfile.mentorId,
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(AppDestination.CourseDetail(courseId))
                },
                onMessageClick = { mentorId ->
                    navigator.navigateTo(AppDestination.ChatScreen(mentorId = mentorId))
                }
            )
        }
    }
    composable<AppDestination.Mentor> {
        TopMentorsScreen(
            onBackClick = { navigator.popBackStack() }
        )
    }
    composable<AppDestination.ArScreen> {
        ArScreen(appNavigator = navigator)
    }
    composable<AppDestination.ViewerScreen> {
        ViewerScreen(appNavigator = navigator)
    }


}
