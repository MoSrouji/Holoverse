package com.example.holoverse.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.ui.home.component.CarouselAdds
import com.example.holoverse.ui.home.component.CourseCard
import com.example.holoverse.ui.home.component.SearchBarSampleV2
import com.example.holoverse.ui.home.component.SubTitle
import com.example.holoverse.ui.home.component.TeacherCard
import com.example.holoverse.ui.home.component.TextListButton
import com.example.holoverse.ui.home.component.TextListTextButton
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.HoloverseTheme
import com.example.holoverse.utils.PreferenceManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit,
    onPopularCoursesClick: () -> Unit,
    onTopMentorClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.onRefresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.background
                )
        ) {

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStartPercent = 0,
                                topEndPercent = 0,
                                bottomEndPercent = 7,
                                bottomStartPercent = 7
                            )
                        )
                        .fillMaxHeight(0.45f)

                ) {
                    SpatialBackground()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.isLoading) {
                                    "...."
                                } else {
                                    "Hello Mr ${uiState.currentUser?.fullName ?: "Error"}"

                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(36.dp)
                                )
                            }


                        }
                        Text(
                            text = "What Would You Like To Learn Today ?",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Search Below",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.padding(bottom = 20.dp))

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            SearchBarSampleV2()
                            Spacer(modifier = Modifier.padding(bottom = 8.dp))
                            CarouselAdds()
                        }
                    }
                }


                Scaffold(


                    floatingActionButton = {
                        AnimatedVisibility(visible = uiState.currentUser?.accountType == UserType.Mentor) {
                            ExtendedFloatingActionButton(
                                onClick = { navController.navigate(AppDestination.CreateCourse) },
                                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                text = { Text(text = "Create New Course") },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }


                ) {
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .verticalScroll(
                                state = rememberScrollState()
                            ),
                    ) {
                        if (uiState.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.padding(bottom = 20.dp))
                        SubTitle(
                            text = R.string.Categories,
                            onSubTitleButtonClick = onCategoryClick
                        )
                        TextListButton()

                        SubTitle(
                            text = R.string.popular_Courses,
                            onSubTitleButtonClick = onPopularCoursesClick
                        )
                        TextListTextButton()
                        Spacer(modifier = Modifier.padding(bottom = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                                .horizontalScroll(
                                    state = rememberScrollState()
                                ),
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            if (uiState.courses.isEmpty() && !uiState.isLoading) {
                                Text(text = "No courses available", modifier = Modifier.padding(16.dp))
                            } else {
                                uiState.courses.forEach { course ->
                                    CourseCard(
                                        course = course,
                                        modifier = Modifier.padding(start = 7.dp)
                                    )
                                }
                            }
                        }

                        SubTitle(
                            text = R.string.top_Mentor,
                            onSubTitleButtonClick = onTopMentorClick
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                                .horizontalScroll(
                                    state = rememberScrollState()
                                ),
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            if (uiState.mentors.isEmpty() && !uiState.isLoading) {
                                Text(text = "No mentors available", modifier = Modifier.padding(16.dp))
                            } else {
                                uiState.mentors.forEach { mentor ->
                                    TeacherCard(
                                        mentor = mentor,
                                        modifier = Modifier.padding(start = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        // Preview with default parameters (hiltViewModel won't work in preview without more setup)
        // You might want to create a stateless version for better previewing
    }
}
