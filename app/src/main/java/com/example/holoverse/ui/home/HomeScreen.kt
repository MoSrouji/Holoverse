package com.example.holoverse.ui.home

import TeacherCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.home.component.CarouselAdds
import com.example.holoverse.ui.home.component.CourseCard
import com.example.holoverse.ui.home.component.FabMenuItem
import com.example.holoverse.ui.home.component.FloatingActionButtonMenu
import com.example.holoverse.ui.home.component.HomeSearchBar
import com.example.holoverse.ui.home.component.SubTitle
import com.example.holoverse.ui.home.component.TextListButton
import com.example.holoverse.ui.home.component.TextListTextButton
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.HoloverseTheme
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appNavigator: AppNavigator,
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit,
    onPopularCoursesClick: () -> Unit,
    onTopMentorClick: () -> Unit,
    onMentorClick: (String) -> Unit,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var isFabVisible by remember { mutableStateOf(true) }

    // Logic to hide/show FAB on scroll
    var lastScrollValue by remember { mutableStateOf(0) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect { currentScroll ->
            if (currentScroll > lastScrollValue + 10) {
                isFabVisible = false
            } else if (currentScroll < lastScrollValue - 10) {
                isFabVisible = true
            }
            lastScrollValue = currentScroll
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
                appNavigator.navigateTo(AppDestination.CreateCourse)
            },
            FabMenuItem(analyticsLabel, Icons.Default.Analytics) {
                /* Navigate to Analytics */
            },
            FabMenuItem(studentsLabel, Icons.Default.Groups) {
                /* Navigate to Students List */
            },
            FabMenuItem(messagesLabel, Icons.Default.Chat) {
                appNavigator.navigateTo(AppDestination.ChatScreen())
            },
            FabMenuItem(announcementsLabel, Icons.Default.Campaign) {
                /* Open Announcement Dialog */
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (uiState.currentUser?.accountType == UserType.Mentor) {
                FloatingActionButtonMenu(
                    visible = isFabVisible,
                    items = fabMenuItems
                )
            }
        }
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
                // Top Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    SpatialBackground(
                        modifier = Modifier.matchParentSize(),
                        darkTheme = !darkTheme
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(getGreeting()),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = uiState.currentUser?.fullName ?: stringResource(R.string.guest),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            IconButton(
                                onClick = { /* Navigate to Notifications */ },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = stringResource(R.string.notifications_desc),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        HomeSearchBar(
                            onSearchClick = { appNavigator.navigateTo(AppDestination.Search) }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        CarouselAdds()
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp) // Space for FAB
                ) {
                    if (uiState.error != null) {
                        Card(
                            modifier = Modifier.padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Tab Toggle for Explore and Your Courses
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val commonShape = RoundedCornerShape(12.dp)
                            SegmentedButton(
                                selected = uiState.selectedTab == HomeTab.Explore,
                                onClick = { viewModel.onTabSelected(HomeTab.Explore) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = 0,
                                    count = 2,
                                    baseShape = commonShape
                                ),
                                icon = {},
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.tab_explore),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            SegmentedButton(
                                selected = uiState.selectedTab == HomeTab.YourCourses,
                                onClick = { viewModel.onTabSelected(HomeTab.YourCourses) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = 1,
                                    count = 2,
                                    baseShape = commonShape
                                ),
                                icon = {},
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.tab_your_courses),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.selectedTab == HomeTab.Explore,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            SectionHeader(
                                titleId = R.string.Categories,
                                onClick = onCategoryClick
                            )
                            TextListButton()

                            // 1. Recommended Courses (Personalized)
                            if (uiState.recommendedCourses.isNotEmpty()) {
                                SectionHeader(
                                    titleId = R.string.for_you_courses,
                                    onClick = { appNavigator.navigateTo(AppDestination.Recommended) }
                                )
                                HorizontalCourseList(
                                    courses = uiState.recommendedCourses,
                                    isLoading = uiState.isLoading,
                                    onCourseClick = { course ->
                                        appNavigator.navigateTo(AppDestination.CourseDetail(course.id))
                                    }
                                )
                            }

                            // 2. Recommended Mentors (Personalized)
                            if (uiState.recommendedMentors.isNotEmpty()) {
                                SectionHeader(
                                    titleId = R.string.for_you_mentor,
                                    onClick = { appNavigator.navigateTo(AppDestination.RecommendedMentors) }
                                )
                                HorizontalMentorList(
                                    mentors = uiState.recommendedMentors,
                                    isLoading = uiState.isLoading,
                                    onMentorClick = onMentorClick
                                )
                            }

                            // 3. Popular Courses (Global)
                            SectionHeader(
                                titleId = R.string.popular_Courses,
                                onClick = onPopularCoursesClick
                            )
                            TextListTextButton()
                            
                            HorizontalCourseList(
                                courses = uiState.courses, 
                                isLoading = uiState.isLoading,
                                onCourseClick = { course ->
                                    appNavigator.navigateTo(AppDestination.CourseDetail(course.id))
                                }
                            )

                            // 4. Top Mentors (Global)
                            SectionHeader(
                                titleId = R.string.top_Mentor,
                                onClick = onTopMentorClick
                            )
                            HorizontalMentorList(
                                mentors = uiState.mentors,
                                isLoading = uiState.isLoading,
                                onMentorClick = onMentorClick
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.selectedTab == HomeTab.YourCourses,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.ongoing_learning),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            EmptyStateText(stringResource(R.string.no_enrolled_courses))
                            Button(
                                onClick = { viewModel.onTabSelected(HomeTab.Explore) },
                                modifier = Modifier.padding(top = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.start_exploring))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(titleId: Int, onClick: () -> Unit) {
    SubTitle(
        text = titleId,
        onSubTitleButtonClick = onClick
    )
}

@Composable
fun HorizontalCourseList(
    courses: List<com.example.holoverse.courses.domain.Courses>,
    isLoading: Boolean,
    onCourseClick: (com.example.holoverse.courses.domain.Courses) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (courses.isEmpty() && !isLoading) {
            EmptyStateText(stringResource(R.string.no_courses_available))
        } else {
            courses.forEach { course ->
                CourseCard(
                    course = course,
                    onClick = { onCourseClick(course) }
                )
            }
        }
    }
}

@Composable
fun HorizontalMentorList(
    mentors: List<com.example.holoverse.auth.domain.entities.User.Mentor>,
    isLoading: Boolean,
    onMentorClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (mentors.isEmpty() && !isLoading) {
            EmptyStateText(stringResource(R.string.no_mentors_found))
        } else {
            mentors.forEach { mentor ->
                TeacherCard(
                    mentor = mentor,
                    onClick = { onMentorClick(mentor.userId!!) }
                )
            }
        }
    }
}

@Composable
fun EmptyStateText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

private fun getGreeting(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> R.string.good_morning
        in 12..16 -> R.string.good_afternoon
        else -> R.string.good_evening
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        HomeScreen(
            appNavigator = AppNavigator(),
            onCategoryClick = {},
            onPopularCoursesClick = {},
            onTopMentorClick = {},
            onMentorClick = {},
            darkTheme = true
        )
    }
}
