package com.example.holoverse.navigation

import TeacherProfileInput
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.holoverse.ui.chat.ChatScreen
import com.example.holoverse.ui.collectUserData.student.screens.StudentPreferenceInfoInput
import com.example.holoverse.ui.collectUserData.student.screens.StudentProfileInput
import com.example.holoverse.ui.collectUserData.teacher.screens.TeacherProfessionalInfoInput
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signin.SignInScreen
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpScreen
import com.example.holoverse.ui.commonPart.profile.EditProfileScreen
import com.example.holoverse.ui.commonPart.profile.ProfileScreen
import com.example.holoverse.ui.commonPart.profile.TermsAndConditionsScreen
import com.example.holoverse.ui.courseDetail.CourseDetailScreen
import com.example.holoverse.ui.home.HomeScreen
import com.example.holoverse.ui.home.coursesList.PopularCoursesScreen
import com.example.holoverse.ui.home.coursesList.RecommendationScreen
import com.example.holoverse.ui.home.mentorsList.RecommendedMentorsScreen
import com.example.holoverse.ui.home.mentorsList.TopMentorsScreen
import com.example.holoverse.ui.mentor.MentorProfileScreen
import com.example.holoverse.ui.search.SearchScreen
import com.example.holoverse.ui.spatialTheme.HoloIntroScreen
import com.example.holoverse.ui.teacherPart.courses.CreateCourseScreen
import com.example.holoverse.ui.three_D_Part.ModelViewModel
import com.example.holoverse.ui.three_D_Part.ar.ArScreen
import com.example.holoverse.ui.three_D_Part.gallery.GalleryScreen
import com.example.holoverse.ui.three_D_Part.viewer.ViewerScreen
import com.example.holoverse.ui.transaction.TransactionScreen
import com.example.holoverse.utils.HoloBottomDock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: AppNavigator,
    isLoggedIn: Boolean = false,
    darkTheme: Boolean
) {
    // Level 3: Centralized Navigation Handler
    LaunchedEffect(Unit) {
        navigator.navigationIntents.collectLatest { intent ->
            when (intent) {
                is NavigationIntent.NavigateBack -> navController.popBackStack()
                is NavigationIntent.NavigateTo -> {
                    navController.navigate(intent.route) {
                        launchSingleTop = true
                        intent.builder(this)
                    }
                }

                is NavigationIntent.NavigateAndPopUpTo -> {
                    navController.navigate(intent.route) {
                        launchSingleTop = true
                        popUpTo(intent.popUpToRoute) {
                            inclusive = intent.inclusive
                        }
                    }
                }
            }
        }
    }

    val sharedState = remember { MutableStateFlow(User.Mentor()) }
    val studentSharedState = remember { MutableStateFlow(User.Student()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isInHomeGraph =
        currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.HomeGraph>() } == true
    val isGalleryScreen = currentDestination?.hasRoute<AppDestination.GalleryScreen>() == true
    val isChatScreen = currentDestination?.hasRoute<AppDestination.ChatScreen>() == true
    val showBottomBar = (isInHomeGraph || isGalleryScreen) && !isChatScreen

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {

                HoloBottomDock(navController = navController, darkTheme = darkTheme)
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
            authGraph(
                navigator,
                mentorState = sharedState,
                studentState = studentSharedState,
                darkTheme = darkTheme
            )
            homeGraph(navigator, navController, darkTheme)
            subGraph(navigator, navController, darkTheme)
            modelGraph(navigator, navController, darkTheme)
        }
    }
}

private fun NavGraphBuilder.authGraph(
    navigator: AppNavigator,
    mentorState: MutableStateFlow<User.Mentor>,
    studentState: MutableStateFlow<User.Student>,
    darkTheme: Boolean
) {
    navigation<AppDestination.AuthGraph>(startDestination = AppDestination.HoloIntro) {
        composable<AppDestination.HoloIntro> {
            HoloIntroScreen(
                onNavigationComplete = { navigator.navigateTo(AppDestination.Login) },
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.Login> {
            SignInScreen(
                onSignUpClick = { navigator.navigateTo(AppDestination.SignUp) },
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeGraph,
                        popUpTo = AppDestination.AuthGraph,
                        inclusive = true
                    )
                },
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.SignUp> {
            SignUpScreen(
                onBackClick = { navigator.popBackStack() },
                onNavigateToTeacherProfile = { navigator.navigateTo(AppDestination.SignUpTeacherProfile) },
                onNavigateToStudentProfile = { navigator.navigateTo(AppDestination.SignUpStudentProfile) },
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        AppDestination.HomeGraph,
                        AppDestination.AuthGraph,
                        true
                    )
                },
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.SignUpTeacherProfile> {
            TeacherProfileInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        AppDestination.HomeGraph,
                        AppDestination.AuthGraph,
                        true
                    )
                },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.SignUpTeacherProfessional> {
            TeacherProfessionalInfoInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        AppDestination.HomeGraph,
                        AppDestination.AuthGraph,
                        true
                    )
                },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.SignUpStudentProfile> {
            StudentProfileInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        AppDestination.HomeGraph,
                        AppDestination.AuthGraph,
                        true
                    )
                },
                studentStates = studentState,
                darkTheme = darkTheme
            )
        }

        composable<AppDestination.SignUpStudentPreference> {
            StudentPreferenceInfoInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        AppDestination.HomeGraph,
                        AppDestination.AuthGraph,
                        true
                    )
                },
                studentStates = studentState,
                darkTheme = darkTheme
            )
        }
    }
}

private fun NavGraphBuilder.homeGraph(
    navigator: AppNavigator,
    navController: NavHostController,
    darkTheme: Boolean
) {
    navigation<AppDestination.HomeGraph>(startDestination = AppDestination.HomeScreen) {
        composable<AppDestination.HomeScreen> {
            HomeScreen(
                darkTheme = darkTheme,
                onNavigateToCreateCourse = { navigator.navigateTo(AppDestination.CreateCourse) },
                onNavigateToChat = { navigator.navigateTo(AppDestination.ChatScreen()) },
                onNavigateToSearch = { navigator.navigateTo(AppDestination.Search) },
                onCategoryClick = { navigator.navigateTo(AppDestination.Category) },
                onPopularCoursesClick = { navigator.navigateTo(AppDestination.PopularCourses) },
                onRecommendedCoursesClick = { navigator.navigateTo(AppDestination.Recommended) },
                onTopMentorClick = { navigator.navigateTo(AppDestination.RecommendedMentors) },
                onTopMentorsListClick = { navigator.navigateTo(AppDestination.TopMentors) },
                onMentorClick = { id: String -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                onCourseClick = { course ->
                    navigator.navigateTo(AppDestination.CourseDetail(course.id))
                },
                onCategorySelected = { cat: String ->
                    if (cat != "All") navigator.navigateTo(AppDestination.CategoryCourses(cat))
                }
            )
        }
        composable<AppDestination.Profile> {
            ProfileScreen(
                onEditProfileClick = { navigator.navigateTo(AppDestination.EditProfile) },
                onTermsAndConditionsClick = { navigator.navigateTo(AppDestination.TermsAndConditions) },
                onSignOutSuccess = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.AuthGraph,
                        popUpTo = AppDestination.HomeGraph,
                        inclusive = true
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.Category> {
            CategoryScreen(
                onCategorySelected = { category ->
                    navigator.navigateTo(AppDestination.CategoryCourses(category))
                },
                darkTheme = darkTheme,
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.CategoryCourses> { backStackEntry ->
            CategoryCoursesScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(AppDestination.CourseDetail(courseId))
                },
                darkTheme = darkTheme
            )
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
            val chat: AppDestination.ChatScreen = backStackEntry.toRoute()
            ChatScreen(
                darkTheme = darkTheme,
                mentorId = chat.mentorId,
                onBackClick = { navigator.popBackStack() }
            )
        }
        composable<AppDestination.EditProfile> {
            EditProfileScreen(
                onBackClick = { navigator.popBackStack() },
                onUpdateSuccess = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.TermsAndConditions> {
            TermsAndConditionsScreen(
                onBackClick = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
    }
}

private fun NavGraphBuilder.subGraph(
    navigator: AppNavigator,
    navController: NavHostController,
    darkTheme: Boolean
) {
    navigation<AppDestination.SubGraph>(startDestination = AppDestination.CreateCourse) {
        composable<AppDestination.CreateCourse> {
            CreateCourseScreen(
                onCourseCreated = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.CreateCourse,
                        inclusive = true
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.Search> {
            SearchScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(
                        AppDestination.CourseDetail(
                            courseId
                        )
                    )
                },
                onMentorClick = { mentorId ->
                    navigator.navigateTo(
                        AppDestination.MentorProfile(
                            mentorId
                        )
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.PopularCourses> {
            PopularCoursesScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(
                        AppDestination.CourseDetail(
                            courseId
                        )
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.Recommended> {
            RecommendationScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(
                        AppDestination.CourseDetail(
                            courseId
                        )
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.TopMentors> {
            TopMentorsScreen(
                onBackClick = { navigator.popBackStack() },
                onMentorClick = { mentorId ->
                    navigator.navigateTo(
                        AppDestination.MentorProfile(
                            mentorId
                        )
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.RecommendedMentors> {
            RecommendedMentorsScreen(
                onBackClick = { navigator.popBackStack() },
                onMentorClick = { mentorId ->
                    navigator.navigateTo(
                        AppDestination.MentorProfile(
                            mentorId
                        )
                    )
                },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.Transactions> {
            TransactionScreen(
                appNavigator = navigator,
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.CourseDetail> { backStackEntry ->
            val course: AppDestination.CourseDetail = backStackEntry.toRoute()
            CourseDetailScreen(
                onBackClick = { navigator.popBackStack() },
                onApplyClick = { /* TODO: Navigate to enrollment/transaction */ },
                darkTheme = darkTheme
            )
        }
        composable<AppDestination.MentorProfile> { backStackEntry ->
            val mentor: AppDestination.MentorProfile = backStackEntry.toRoute()
            MentorProfileScreen(
                mentorId = mentor.mentorId,
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { id -> navigator.navigateTo(AppDestination.CourseDetail(id)) },
                onMessageClick = { id -> navigator.navigateTo(AppDestination.ChatScreen(id)) },
                darkTheme = darkTheme
            )
        }
    }
}

private fun NavGraphBuilder.modelGraph(
    navigator: AppNavigator,
    navController: NavHostController,
    darkTheme: Boolean
) {
    navigation<AppDestination.ModelGraph>(startDestination = AppDestination.GalleryScreen) {
        composable<AppDestination.GalleryScreen> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppDestination.ModelGraph)
            }
            val viewModel: ModelViewModel = hiltViewModel(parentEntry)
            GalleryScreen(
                appNavigator = navigator,
                darkTheme = darkTheme,
                viewModel = viewModel
            )
        }
        composable<AppDestination.ViewerScreen> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppDestination.ModelGraph)
            }
            val viewModel: ModelViewModel = hiltViewModel(parentEntry)
            ViewerScreen(
                appNavigator = navigator,
                viewModel = viewModel
            )
        }
        composable<AppDestination.ArScreen> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppDestination.ModelGraph)
            }
            val viewModel: ModelViewModel = hiltViewModel(parentEntry)
            ArScreen(
                appNavigator = navigator,
                viewModel = viewModel
            )
        }
    }
}
