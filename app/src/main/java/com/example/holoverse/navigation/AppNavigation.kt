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
import com.example.holoverse.auth.domain.entities.User
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
import com.example.holoverse.ui.studentPart.mentors.TopMentorsScreen
import com.example.holoverse.ui.teacherPart.courses.CreateCourseScreen
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.compose.currentStateAsState
import com.example.holoverse.ui.chat.ChatScreen
import com.example.holoverse.ui.commonPart.profile.EditProfileScreen
import com.example.holoverse.ui.studentPart.popularCourses.PopularCoursesScreen
import com.example.holoverse.ui.transaction.TransactionScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: AppNavigator
) {
    navigator.init(navController)

    val sharedState = MutableStateFlow(User.Teacher())

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Logic to show bottom bar after transition completes
    val isInHomeGraph =
        currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.HomeGraph>() } == true
    val isDestinationResumed =
        navBackStackEntry?.lifecycle?.currentStateAsState()?.value == Lifecycle.State.RESUMED

    var showBottomBar by remember { mutableStateOf(false) }

    LaunchedEffect(isInHomeGraph, isDestinationResumed) {
        if (isInHomeGraph && isDestinationResumed) {
            showBottomBar = true
        } else if (!isInHomeGraph) {
            showBottomBar = false
        }
    }

    SpatialBackground()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                HoloBottomDock(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.AuthGraph,
            modifier = Modifier.padding(innerPadding)
        ) {
            authGraph(navigator, teacherState = sharedState)
            homeGraph(navigator)
            subGraph(navigator)
        }
    }
}

private fun NavGraphBuilder.authGraph(
    navigator: AppNavigator,
    teacherState: MutableStateFlow<User.Teacher>
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
                teacherStates = teacherState
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
                teacherStates = teacherState
            )
        }
    }
}

private fun NavGraphBuilder.homeGraph(
    navigator: AppNavigator,
) {
    navigation<AppDestination.HomeGraph>(
        startDestination = AppDestination.HomeScreen
    ) {
        composable<AppDestination.HomeScreen>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            HomeScreen(
                navController = navigator.navController,
                onCategoryClick = {
                    navigator.navigateTo(destination = AppDestination.Category)
                },
                onPopularCoursesClick = {
                    navigator.navigateTo(destination = AppDestination.PopularCourses)
                },
                onTopMentorClick = {
                    navigator.navigateTo(destination = AppDestination.Mentor)
                }
            )
        }
        composable<AppDestination.Profile>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            ProfileScreen(navController = navigator)
        }

        composable<AppDestination.Category>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            CategoryScreen()
        }
        composable<AppDestination.Transactions>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            TransactionScreen()
        }
        composable<AppDestination.ChatScreen>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            ChatScreen()
        }
    }
}

private fun NavGraphBuilder.subGraph(
    navigator: AppNavigator
) {
    navigation<AppDestination.SubGraph>(
        startDestination = AppDestination.CreateCourse
    ) {
        composable<AppDestination.CreateCourse>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            CreateCourseScreen(
                onNavigateBack = {
                    navigator.popBackStack()
                }
            )
        }
        composable<AppDestination.Search>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            SearchScreen()
        }
        composable<AppDestination.EditProfile>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            EditProfileScreen()
        }
        composable<AppDestination.PopularCourses>(
            enterTransition = { NavAnimations.slideInFromRight() }
        ) {
            PopularCoursesScreen()
        }
    }
    composable<AppDestination.Mentor>(
        enterTransition = { NavAnimations.slideInFromRight() }
    ) {
        TopMentorsScreen()
    }
}
