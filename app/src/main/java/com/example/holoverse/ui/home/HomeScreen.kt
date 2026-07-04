package com.example.holoverse.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.home.component.FabMenuItem
import com.example.holoverse.ui.home.component.FloatingActionButtonMenu
import com.example.holoverse.ui.home.component.HomeContentSections
import com.example.holoverse.ui.home.component.HomeScreenHeader
import com.example.holoverse.ui.home.component.HomeTabRow
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.theme.HoloverseTheme

private const val SCROLL_THRESHOLD = 10

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit,
    onPopularCoursesClick: () -> Unit,
    onRecommendedCoursesClick: () -> Unit,
    onTopMentorClick: () -> Unit,
    onTopMentorsListClick: () -> Unit,
    onMentorClick: (String) -> Unit,
    onNavigateToCreateCourse: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToStudentsList: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSearch: (Boolean) -> Unit,
    onCourseClick: (Courses) -> Unit,
    onCategorySelected: (AppCategory) -> Unit,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.onRefresh() },
        onTabSelected = viewModel::onTabSelected,
        onCategorySelected = {
            viewModel.onCategorySelected(it)
            onCategorySelected(it)
        },
        onFilterCategorySelected = viewModel::onCategorySelected,
        onSaveCourseClick = viewModel::toggleSaveCourse,
        onCategoryClick = onCategoryClick,
        onPopularCoursesClick = onPopularCoursesClick,
        onRecommendedCoursesClick = onRecommendedCoursesClick,
        onTopMentorClick = onTopMentorClick,
        onTopMentorsListClick = onTopMentorsListClick,
        onMentorClick = onMentorClick,
        onNavigateToCreateCourse = onNavigateToCreateCourse,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToStudentsList = onNavigateToStudentsList,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToChat = onNavigateToChat,
        onNavigateToSearch = onNavigateToSearch,
        onCourseClick = onCourseClick,
        darkTheme = darkTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    onCategorySelected: (AppCategory) -> Unit,
    onFilterCategorySelected: (AppCategory) -> Unit,
    onSaveCourseClick: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onPopularCoursesClick: () -> Unit,
    onRecommendedCoursesClick: () -> Unit,
    onTopMentorClick: () -> Unit,
    onTopMentorsListClick: () -> Unit,
    onMentorClick: (String) -> Unit,
    onNavigateToCreateCourse: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToStudentsList: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSearch: (Boolean) -> Unit,
    onCourseClick: (Courses) -> Unit,
    darkTheme: Boolean
) {
    val scrollState = rememberScrollState()

    // Optimized FAB visibility logic using a simpler scroll-direction detection
    var lastScrollValue by remember { mutableIntStateOf(0) }
    var fabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentScroll ->
                val delta = currentScroll - lastScrollValue
                if (delta > SCROLL_THRESHOLD) {
                    fabVisible = false
                } else if (delta < -SCROLL_THRESHOLD || currentScroll <= 0) {
                    fabVisible = true
                }
                lastScrollValue = currentScroll
            }
    }

    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

    val createCourseLabel = stringResource(R.string.create_course)
    val analyticsLabel = stringResource(R.string.analytics)
    val studentsLabel = stringResource(R.string.students)
    val messagesLabel = stringResource(R.string.messages)
    val announcementsLabel = stringResource(R.string.announcements)

    val fabMenuItems = remember(
        createCourseLabel,
        analyticsLabel,
        studentsLabel,
        messagesLabel,
        announcementsLabel
    ) {
        listOf(
            FabMenuItem(createCourseLabel, Icons.Default.Add) {
                onNavigateToCreateCourse()
            },
            FabMenuItem(analyticsLabel, Icons.Default.Analytics) {
                onNavigateToAnalytics()
            },
            FabMenuItem(studentsLabel, Icons.Default.Groups) {
                onNavigateToStudentsList()
            },
            FabMenuItem(messagesLabel, Icons.Default.Chat) {
                onNavigateToChat()
            },
            FabMenuItem(announcementsLabel, Icons.Default.Campaign) {
                /* Open Announcement Dialog */
            }
        )
    }

    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        floatingActionButton = {
            if (uiState.currentUser?.accountType == UserType.Mentor) {
                Box(
                    modifier = Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                ) {
                    FloatingActionButtonMenu(
                        visible = fabVisible,
                        items = fabMenuItems
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isLoading,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                HomeScreenHeader(
                    fullName = uiState.currentUser?.fullName,
                    isLoading = uiState.isLoading,
                    darkTheme = darkTheme,
                    boostedCourses = uiState.boostedCourses,
                    allCourses = uiState.allCourses,
                    onCourseClick = onCourseClick,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = onNavigateToNotifications,
                    brush = { headerBrush }
                )

                // Error and Offline Handling
                if (uiState.error != null) {
                    ErrorCard(
                        error = uiState.error,
                        onRetry = onRefresh
                    )
                }

                if (uiState.isOffline) {
                    OfflineBanner()
                }

                HomeTabRow(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )

                HomeContentSections(
                    uiState = uiState,
                    onTabSelected = onTabSelected,
                    onCategorySelected = onCategorySelected,
                    onFilterCategorySelected = onFilterCategorySelected,
                    onCategoryClick = onCategoryClick,
                    onPopularCoursesClick = onPopularCoursesClick,
                    onRecommendedCoursesClick = onRecommendedCoursesClick,
                    onTopMentorClick = onTopMentorClick,
                    onTopMentorsListClick = onTopMentorsListClick,
                    onMentorClick = onMentorClick,
                    onCourseClick = onCourseClick,
                    onSaveCourseClick = onSaveCourseClick
                )
            }
            // Extra spacer to ensure content isn't hidden by FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ErrorCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
fun OfflineBanner() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.offline_mode),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews

@FormFactorPreviews
@Composable
fun HomeScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        HomeScreenContent(
            uiState = HomeUiState(),
            onRefresh = {},
            onTabSelected = {},
            onCategorySelected = {},
            onFilterCategorySelected = {},
            onSaveCourseClick = {},
            onCategoryClick = {},
            onPopularCoursesClick = {},
            onRecommendedCoursesClick = {},
            onTopMentorClick = {},
            onTopMentorsListClick = {},
            onMentorClick = {},
            onNavigateToCreateCourse = {},
            onNavigateToAnalytics = {},
            onNavigateToStudentsList = {},
            onNavigateToNotifications = {},
            onNavigateToChat = {},
            onNavigateToSearch = {},
            onCourseClick = {},
            darkTheme = true
        )
    }
}

