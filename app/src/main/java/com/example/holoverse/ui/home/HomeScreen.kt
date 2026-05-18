package com.example.holoverse.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.home.component.*
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.theme.HoloverseTheme

private const val SCROLL_THRESHOLD = 10

@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToChat: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onCourseClick: (Courses) -> Unit,
    onCategorySelected: (String) -> Unit,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var isFabVisible by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Logic to hide/show FAB on scroll with lifecycle awareness
    LaunchedEffect(scrollState, lifecycleOwner) {
        var lastScrollValue = 0
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            snapshotFlow { scrollState.value }.collect { currentScroll ->
                if (currentScroll > lastScrollValue + SCROLL_THRESHOLD) {
                    isFabVisible = false
                } else if (currentScroll < lastScrollValue - SCROLL_THRESHOLD) {
                    isFabVisible = true
                }
                lastScrollValue = currentScroll
            }
        }
    }

    val createCourseLabel = stringResource(R.string.create_course)
    val analyticsLabel = stringResource(R.string.analytics)
    val studentsLabel = stringResource(R.string.students)
    val messagesLabel = stringResource(R.string.messages)
    val announcementsLabel = stringResource(R.string.announcements)

    val fabMenuItems = remember(createCourseLabel, analyticsLabel, studentsLabel, messagesLabel, announcementsLabel) {
        listOf(
            FabMenuItem(createCourseLabel, Icons.Default.Add) {
                onNavigateToCreateCourse()
            },
            FabMenuItem(analyticsLabel, Icons.Default.Analytics) {
                /* Navigate to Analytics */
            },
            FabMenuItem(studentsLabel, Icons.Default.Groups) {
                /* Navigate to Students List */
            },
            FabMenuItem(messagesLabel, Icons.Default.Chat) {
                onNavigateToChat()
            },
            FabMenuItem(announcementsLabel, Icons.Default.Campaign) {
                /* Open Announcement Dialog */
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (uiState.currentUser?.accountType == UserType.Mentor) {
                Box(modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    FloatingActionButtonMenu(
                        visible = isFabVisible,
                        items = fabMenuItems
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onRefresh() },
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
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = { /* Navigate to Notifications */ },
                    brush = { Brush(it) }
                )

                // Error and Offline Handling
                if (uiState.error != null) {
                    ErrorCard(
                        error = uiState.error ?: "",
                        onRetry = { viewModel.onRefresh() }
                    )
                }

                if (uiState.isOffline) {
                    OfflineBanner()
                }

                HomeTabRow(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected
                )

                    HomeContentSections(
                        uiState = uiState,
                        onTabSelected = viewModel::onTabSelected,
                        onCategorySelected = {
                            viewModel.onCategorySelected(it)
                            onCategorySelected(it)
                        },
                        onFilterCategorySelected = viewModel::onCategorySelected,
                        onCategoryClick = onCategoryClick,
                        onPopularCoursesClick = onPopularCoursesClick,
                        onRecommendedCoursesClick = onRecommendedCoursesClick,
                        onTopMentorClick = onTopMentorClick,
                        onTopMentorsListClick = onTopMentorsListClick,
                        onMentorClick = onMentorClick,
                        onCourseClick = onCourseClick
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

@Composable
@Preview
fun HomeScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        HomeScreen(
            onCategoryClick = {},
            onPopularCoursesClick = {},
            onRecommendedCoursesClick = {},
            onTopMentorClick = {},
            onTopMentorsListClick = {},
            onMentorClick = {},
            onNavigateToCreateCourse = {},
            onNavigateToChat = {},
            onNavigateToSearch = {},
            onCourseClick = {},
            onCategorySelected = {},
            darkTheme = true
        )
    }


}

