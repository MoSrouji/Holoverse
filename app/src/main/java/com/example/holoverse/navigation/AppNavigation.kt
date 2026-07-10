package com.example.holoverse.navigation

import TeacherProfileInput
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.holoverse.R
import com.example.holoverse.admin.presentation.screen.AdminControlPanelScreen
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.chat.presentation.ChatScreen
import com.example.holoverse.chat.presentation.ChatViewModel
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.notifications.presentation.NotificationScreen
import com.example.holoverse.threedmodel.presentation.ModelViewModel
import com.example.holoverse.course.presentation.category.CategoryCoursesScreen
import com.example.holoverse.course.presentation.category.CategoryScreen
import com.example.holoverse.auth.presentation.profile_setup.student.StudentPreferenceInfoInput
import com.example.holoverse.auth.presentation.profile_setup.student.StudentProfileInput
import com.example.holoverse.auth.presentation.profile_setup.teacher.TeacherProfessionalInfoInput
import com.example.holoverse.auth.presentation.login.SignInScreen
import com.example.holoverse.auth.presentation.signup.SignUpScreen
import com.example.holoverse.ui.commonpart.profile.ChangePasswordScreen
import com.example.holoverse.ui.commonpart.profile.EditProfileScreen
import com.example.holoverse.ui.commonpart.profile.ProfileScreen
import com.example.holoverse.ui.commonpart.profile.TermsAndConditionsScreen
import com.example.holoverse.course.presentation.detail.CourseDetailScreen
import com.example.holoverse.ui.home.HomeScreen
import com.example.holoverse.ui.home.courseslist.PopularCoursesScreen
import com.example.holoverse.ui.home.courseslist.RecommendationScreen
import com.example.holoverse.ui.home.mentorslist.RecommendedMentorsScreen
import com.example.holoverse.ui.home.mentorslist.TopMentorsScreen
import com.example.holoverse.ui.mentor.MentorProfileScreen
import com.example.holoverse.ui.mentor.analysis.MentorAnalysisScreen
import com.example.holoverse.ui.mentor.announcements.AnnouncementsScreen
import com.example.holoverse.ui.search.SearchScreen
import com.example.holoverse.ui.spatialtheme.Brush
import com.example.holoverse.ui.spatialtheme.HoloIntroScreen
import com.example.holoverse.course.presentation.creation.CreateCourseScreen
import com.example.holoverse.course.presentation.students.StudentListScreen
import com.example.holoverse.ui.three_D_Part.ar.ArScreen
import com.example.holoverse.ui.three_D_Part.gallery.GalleryScreen
import com.example.holoverse.ui.three_D_Part.viewer.ViewerScreen
import com.example.holoverse.ui.transaction.TransactionScreen
import com.example.holoverse.webrtc.presentation.IncomingCallScreen
import com.example.holoverse.webrtc.presentation.OutgoingCallScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

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
        startRoute = startRoute,
        topLevelRoutes = topLevelRoutes
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
                    nav3Navigator.navigateAndPopUpTo(
                        route = intent.route,
                        popUpTo = intent.popUpToRoute,
                        inclusive = intent.inclusive
                    )
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

    var bottomBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute, isCompact) {
        val shouldShow = when (currentRoute) {
            is AppDestination.HomeScreen, is AppDestination.Category, is AppDestination.ChatList, is AppDestination.GalleryScreen, is AppDestination.Profile -> true
            is AppDestination.ChatScreen, is AppDestination.OutgoingCall -> !isCompact
            else -> false
        }

        if (shouldShow) {
            delay(300.milliseconds)
            bottomBarVisible = true
        } else {
            bottomBarVisible = false
        }
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
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUp> {
            SignUpScreen(
                onBackClick = { navigator.popBackStack() },
                onNavigateToTeacherProfile = { navigator.navigateTo(AppDestination.SignUpTeacherProfile) },
                onNavigateToStudentProfile = { navigator.navigateTo(AppDestination.SignUpStudentProfile) },
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpTeacherProfile> {
            TeacherProfileInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpTeacherProfessional> {
            TeacherProfessionalInfoInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
                mentorStates = mentorState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpStudentProfile> {
            StudentProfileInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
                studentStates = studentState,
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.SignUpStudentPreference> {
            StudentPreferenceInfoInput(
                navController = navigator,
                navToHomeScreen = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.HomeScreen,
                        popUpTo = AppDestination.HoloIntro,
                        inclusive = true
                    )
                },
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
                onNavigateToStudentsList = { navigator.navigateTo(AppDestination.StudentsList) },
                onNavigateToNotifications = { navigator.navigateTo(AppDestination.Notifications) },
                onNavigateToAnnouncements = { navigator.navigateTo(AppDestination.Announcements) }
            )
        }
        entry<AppDestination.Profile> {
            ProfileScreen(
                onEditProfileClick = {
                    navigator.navigateTo(
                        AppDestination.EditProfile
                    )
                },
                onAdminClick = { navigator.navigateTo(AppDestination.AdminControlPanel) },
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
                        Text(stringResource(R.string.select_chat_placeholder))
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
            val viewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                darkTheme = darkTheme,
                mentorId = key.mentorId,
                viewModel = viewModel,
                onBackClick = { navigator.popBackStack() },
                onNavigateToVideoCall = { callId, partnerName, partnerImageUrl ->
                    viewModel.startVideoCall(callId, partnerName, partnerImageUrl)
                    navigator.navigateTo(
                        AppDestination.OutgoingCall(
                            callId = callId,
                            receiverName = partnerName,
                            receiverImageUrl = partnerImageUrl
                        )
                    )
                },
                onIncomingCall = { chatId, partnerName, partnerImageUrl ->
                    navigator.navigateTo(
                        AppDestination.IncomingCall(
                            callId = chatId,
                            callerName = partnerName,
                            callerImageUrl = partnerImageUrl
                        )
                    )
                })
        }
        entry<AppDestination.OutgoingCall> { key: AppDestination.OutgoingCall ->
            OutgoingCallScreen(
                callId = key.callId,
                receiverName = key.receiverName,
                receiverImageUrl = key.receiverImageUrl,
                onCallConnected = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.VideoCall(key.callId, isOffer = true),
                        popUpTo = key,
                        inclusive = true
                    )
                },
                onEndCall = {
                    navigator.popBackStack()
                }
            )
        }
        entry<AppDestination.IncomingCall> { key: AppDestination.IncomingCall ->
            IncomingCallScreen(
                callId = key.callId,
                callerName = key.callerName,
                callerImageUrl = key.callerImageUrl,
                onAnswer = {
                    navigator.navigateAndPopUpTo(
                        destination = AppDestination.VideoCall(key.callId, isOffer = false),
                        popUpTo = key,
                        inclusive = true
                    )
                },
                onDecline = {
                    navigator.popBackStack()
                }
            )
        }
        entry<AppDestination.VideoCall> { key: AppDestination.VideoCall ->
            com.example.holoverse.webrtc.presentation.VideoCallScreen(
                callId = key.callId, isOffer = key.isOffer,
                onCallEnded = { navigator.popBackStack() })
        }
        entry<AppDestination.EditProfile> {
            EditProfileScreen(
                onBackClick = { navigator.popBackStack() },
                onChangePasswordClick = { navigator.navigateTo(AppDestination.ChangePassword) },
                onUpdateSuccess = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.ChangePassword> {
            ChangePasswordScreen(
                onBackClick = { navigator.popBackStack() },
                onSuccess = { navigator.popBackStack() },
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
        entry<AppDestination.Notifications> {
            NotificationScreen(
                onBackClick = { navigator.popBackStack() },
                onNotificationClick = { notification ->
                    if (notification.type == "course_created" && notification.courseId != null) {
                        navigator.navigateTo(AppDestination.CourseDetail(notification.courseId))
                    }
                },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.Announcements> {
            AnnouncementsScreen(
                onBackClick = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
        }
        entry<AppDestination.AdminControlPanel> {
            AdminControlPanelScreen(
                onBackClick = { navigator.popBackStack() },
                darkTheme = darkTheme
            )
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
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    modifier = Modifier
                        .height(56.dp)
                        .background(Brush(darkTheme)),
                    containerColor = Color.Transparent,
                ) {
                    val items = listOf(
                        Triple(
                            AppDestination.Category,
                            Icons.AutoMirrored.Default.ViewList,
                            "Categories"
                        ),
                        Triple(
                            AppDestination.ChatList,
                            Icons.AutoMirrored.Filled.Message,
                            "Messages"
                        ),
                        Triple(AppDestination.HomeScreen, Icons.Default.Home, "Home"),
                        Triple(AppDestination.GalleryScreen, Icons.Default.Storefront, "Gallery"),
                        Triple(AppDestination.Profile, Icons.Default.PersonOutline, "Profile")
                    )

                    items.forEach { (destination, icon, label) ->
                        val isSelected = navigationState.topLevelRoute == destination
                        NavigationBarItem(
                            modifier = Modifier

                                .height(44.dp),
                            selected = isSelected,
                            onClick = { navigator.navigateTo(destination) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
//                            label = {
//                                Text(
//                                    text = label,
//                                    style = MaterialTheme.typography.labelSmall
//                                )
//                            },
//                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.secondary,
                                selectedTextColor = MaterialTheme.colorScheme.secondary,
                                unselectedIconColor = if (darkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(
                                    alpha = 0.4f
                                ),
                                unselectedTextColor = if (darkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(
                                    alpha = 0.4f
                                ),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
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


