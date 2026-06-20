package com.example.holoverse.navigation

import TeacherProfileInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
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
import com.example.holoverse.ui.mentor.analysis.MentorAnalysisScreen
import com.example.holoverse.ui.search.SearchScreen
import com.example.holoverse.ui.spatialTheme.HoloIntroScreen
import com.example.holoverse.ui.teacherPart.courses.CreateCourseScreen
import com.example.holoverse.ui.teacherPart.students.StudentListScreen
import com.example.holoverse.ui.three_D_Part.ModelViewModel
import com.example.holoverse.ui.three_D_Part.ar.ArScreen
import com.example.holoverse.ui.three_D_Part.gallery.GalleryScreen
import com.example.holoverse.ui.three_D_Part.viewer.ViewerScreen
import com.example.holoverse.ui.transaction.TransactionScreen
import com.example.holoverse.utils.HoloBottomDock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavHost(
    navigator: AppNavigator,
    isLoggedIn: Boolean = false,
    darkTheme: Boolean,
) {
    val startRoute = if (isLoggedIn) AppDestination.HomeScreen else AppDestination.HoloIntro
    val topLevelRoutes = setOf(
        AppDestination.HoloIntro,
        AppDestination.HomeScreen,
        AppDestination.ChatList,
        AppDestination.GalleryScreen,
        AppDestination.Profile,
        AppDestination.Category
    )

    val navigationState = rememberNavigationState(
        startRoute = startRoute, topLevelRoutes = topLevelRoutes
    )
    val nav3Navigator = remember { Navigator(navigationState) }

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    LaunchedEffect(Unit) {
        navigator.navigationIntents.collectLatest { intent ->
            when (intent) {
                is NavigationIntent.NavigateBack -> nav3Navigator.goBack()
                is NavigationIntent.NavigateTo -> nav3Navigator.navigate(intent.route)
                is NavigationIntent.NavigateAndPopUpTo -> {
                    // Simple implementation for now, might need more logic for popUpTo
                    nav3Navigator.navigate(intent.route)
                }
            }
        }
    }

    val mentorState = remember { MutableStateFlow(User.Mentor()) }
    val studentState = remember { MutableStateFlow(User.Student()) }

    val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.last()
        ?: navigationState.topLevelRoute

    val isCompact = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass.toString()
        .contains("COMPACT", ignoreCase = true)

    val showBottomBar = when (currentRoute) {
        is AppDestination.HomeScreen, is AppDestination.Category, is AppDestination.ChatList, is AppDestination.GalleryScreen, is AppDestination.Profile -> true

        is AppDestination.ChatScreen -> !isCompact

        else -> false
    }

    val entryProvider = entryProvider<NavKey> {
        // Auth
        entry<AppDestination.HoloIntro> {
            HoloIntroScreen(onNavigationComplete = {
                navigator.navigateTo(
                    AppDestination.Login
                )
            }, darkTheme = darkTheme)
        }
        entry<AppDestination.Login> {
            SignInScreen(
                onSignUpClick = {
                    navigator.navigateTo(
                        AppDestination.SignUp
                    )
                },
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUp> {
            SignUpScreen(
                onBackClick = { navigator.popBackStack() },
                onNavigateToTeacherProfile = { navigator.navigateTo(AppDestination.SignUpTeacherProfile) },
                onNavigateToStudentProfile = { navigator.navigateTo(AppDestination.SignUpStudentProfile) },
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpTeacherProfile> {
            TeacherProfileInput(
                navController = navigator,
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpTeacherProfessional> {
            TeacherProfessionalInfoInput(
                navController = navigator,
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpStudentProfile> {
            StudentProfileInput(
                navController = navigator,
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                studentStates = studentState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpStudentPreference> {
            StudentPreferenceInfoInput(
                navController = navigator,
                navToHomeScreen = { navigator.navigateTo(AppDestination.HomeScreen) },
                studentStates = studentState,
                darkTheme = darkTheme
            )
        }

        // Home & Main
        entry<AppDestination.HomeScreen> {
            HomeScreen(
                darkTheme = darkTheme,
                onNavigateToCreateCourse = { navigator.navigateTo(AppDestination.CreateCourse) },
                onNavigateToAnalytics = { navigator.navigateTo(AppDestination.MentorAnalysis) },
                onNavigateToChat = { navigator.navigateTo(AppDestination.ChatList) },
                onNavigateToSearch = { triggerVoice ->
                    navigator.navigateTo(
                        AppDestination.Search(triggerVoice)
                    )
                },
                onCategoryClick = { navigator.navigateTo(AppDestination.Category) },
                onPopularCoursesClick = { navigator.navigateTo(AppDestination.PopularCourses) },
                onRecommendedCoursesClick = { navigator.navigateTo(AppDestination.Recommended) },
                onTopMentorClick = { navigator.navigateTo(AppDestination.RecommendedMentors) },
                onTopMentorsListClick = { navigator.navigateTo(AppDestination.TopMentors) },
                onMentorClick = { id -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                onCourseClick = { course -> navigator.navigateTo(AppDestination.CourseDetail(course.id)) },
                onCategorySelected = { cat ->
                    if (cat != AppCategory.OTHER) navigator.navigateTo(
                        AppDestination.CategoryCourses(
                            cat
                        )
                    )
                },
                onNavigateToStudentsList = { navigator.navigateTo(AppDestination.StudentsList) })
        }
        entry<AppDestination.Profile> {
            ProfileScreen(
                onEditProfileClick = {
                    navigator.navigateTo(
                        AppDestination.EditProfile
                    )
                },
                onTermsAndConditionsClick = { navigator.navigateTo(AppDestination.TermsAndConditions) },
                onSignOutSuccess = { navigator.navigateTo(AppDestination.HoloIntro) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.Category> {
            CategoryScreen(onCategorySelected = { category ->
                navigator.navigateTo(
                    AppDestination.CategoryCourses(category)
                )
            }, darkTheme = darkTheme, onBackClick = { navigator.popBackStack() })
        }
        entry<AppDestination.CategoryCourses> { key: AppDestination.CategoryCourses ->
            CategoryCoursesScreen(
                category = key.category,
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { courseId ->
                    navigator.navigateTo(
                        AppDestination.CourseDetail(courseId)
                    )
                },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.ChatList>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Text("Select a chat to start messaging")
                    }
                })
        ) {
            ChatScreen(
                darkTheme = darkTheme, onNavigateToConversation = { mentorId ->
                    navigator.navigateTo(
                        AppDestination.ChatScreen(mentorId = mentorId)
                    )
                })
        }
        entry<AppDestination.ChatScreen>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) { key: AppDestination.ChatScreen ->
            ChatScreen(
                darkTheme = darkTheme,
                mentorId = key.mentorId,
                onBackClick = { navigator.popBackStack() },
                onNavigateToVideoCall = { callId ->
                    navigator.navigateTo(AppDestination.VideoCall(callId, isOffer = true))
                },
                onIncomingCall = { chatId ->
                    navigator.navigateTo(
                        AppDestination.VideoCall(
                            chatId, isOffer = false
                        )
                    )
                })
        }
        entry<AppDestination.VideoCall> { key: AppDestination.VideoCall ->
            com.example.holoverse.webrtc.presentation.VideoCallScreen(
                callId = key.callId, isOffer = key.isOffer,
                onCallEnded = { navigator.popBackStack() })
        }
        entry<AppDestination.EditProfile> {
            EditProfileScreen(
                onBackClick = { navigator.popBackStack() },
                onUpdateSuccess = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.TermsAndConditions> {
            TermsAndConditionsScreen(
                onBackClick = { navigator.popBackStack() }, darkTheme = darkTheme
            )
        }

        // SubGraph
        entry<AppDestination.CreateCourse> {
            CreateCourseScreen(
                onCourseCreated = { navigator.popBackStack() }, darkTheme = darkTheme
            )
        }
        entry<AppDestination.Search> { key: AppDestination.Search ->
            SearchScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { id -> navigator.navigateTo(AppDestination.CourseDetail(id)) },
                onMentorClick = { id -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                darkTheme = darkTheme,
                triggerVoice = key.triggerVoice
            )
        }
        entry<AppDestination.PopularCourses> {
            PopularCoursesScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { id -> navigator.navigateTo(AppDestination.CourseDetail(id)) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.Recommended> {
            RecommendationScreen(
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { id -> navigator.navigateTo(AppDestination.CourseDetail(id)) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.TopMentors> {
            TopMentorsScreen(
                onBackClick = { navigator.popBackStack() },
                onMentorClick = { id -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.RecommendedMentors> {
            RecommendedMentorsScreen(
                onBackClick = { navigator.popBackStack() },
                onMentorClick = { id -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.Transactions> {
            TransactionScreen(
                appNavigator = navigator, darkTheme = darkTheme
            )
        }
        entry<AppDestination.CourseDetail> { key: AppDestination.CourseDetail ->
            CourseDetailScreen(
                courseId = key.courseId,
                onBackClick = { navigator.popBackStack() },
                onInstructorClick = { id -> navigator.navigateTo(AppDestination.MentorProfile(id)) },
                onEnrollSuccess = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.MentorProfile> { key: AppDestination.MentorProfile ->
            MentorProfileScreen(
                mentorId = key.mentorId,
                onBackClick = { navigator.popBackStack() },
                onCourseClick = { id -> navigator.navigateTo(AppDestination.CourseDetail(id)) },
                onMessageClick = { id -> navigator.navigateTo(AppDestination.ChatScreen(id)) },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.MentorAnalysis> {
            MentorAnalysisScreen(onBackClick = { navigator.popBackStack() }, darkTheme = darkTheme)
        }
        entry<AppDestination.StudentsList> {
            StudentListScreen(
                onBackClick = { navigator.popBackStack() },
                onStudentClick = { studentId ->
                    navigator.navigateTo(AppDestination.MentorProfile(studentId))
                })
        }

        // ModelGraph
        entry<AppDestination.GalleryScreen> {
            val viewModel: ModelViewModel = hiltViewModel()
            GalleryScreen(appNavigator = navigator, darkTheme = darkTheme, viewModel = viewModel)
        }
        entry<AppDestination.ViewerScreen> {
            val viewModel: ModelViewModel = hiltViewModel()
            ViewerScreen(appNavigator = navigator, viewModel = viewModel)
        }
        entry<AppDestination.ArScreen> {
            val viewModel: ModelViewModel = hiltViewModel()
            ArScreen(appNavigator = navigator, viewModel = viewModel)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HoloBottomDock(
                    navigationState = navigationState, navigator = navigator, darkTheme = darkTheme
                )
            }
        }) { padding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            entries = navigationState.toEntries(entryProvider),
            onBack = { nav3Navigator.goBack() },
            sceneStrategies = listOf(listDetailStrategy),
            transitionSpec = { NavAnimations.forward() },
            popTransitionSpec = { NavAnimations.backward() },
            predictivePopTransitionSpec = { NavAnimations.backward() })
    }
}
