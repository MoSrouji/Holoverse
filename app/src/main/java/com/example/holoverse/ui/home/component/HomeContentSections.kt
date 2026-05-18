package com.example.holoverse.ui.home.component

import TeacherCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.home.HomeTab
import com.example.holoverse.ui.home.HomeUiState

@Composable
fun HomeContentSections(
    uiState: HomeUiState,
    onTabSelected: (HomeTab) -> Unit,
    onCategorySelected: (String) -> Unit,
    onFilterCategorySelected: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onPopularCoursesClick: () -> Unit,
    onRecommendedCoursesClick: () -> Unit,
    onTopMentorClick: () -> Unit,
    onTopMentorsListClick: () -> Unit,
    onMentorClick: (String) -> Unit,
    onCourseClick: (Courses) -> Unit
) {
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
            TextListButton(
                categories = uiState.categories,
                isLoading = uiState.isLoading,
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = onCategorySelected
            )

            // 1. Recommended Courses (Personalized)
            SectionHeader(
                titleId = R.string.for_you_courses,
                onClick = onRecommendedCoursesClick
            )
            HorizontalCourseList(
                courses = uiState.recommendedCourses,
                isLoading = uiState.isLoading,
                onCourseClick = onCourseClick
            )

            // 2. Recommended Mentors (Personalized)
            SectionHeader(
                titleId = R.string.for_you_mentor,
                onClick = onTopMentorClick
            )
            HorizontalMentorList(
                mentors = uiState.recommendedMentors,
                isLoading = uiState.isLoading,
                onMentorClick = onMentorClick
            )

            // 3. Popular Courses (Global)
            SectionHeader(
                titleId = R.string.popular_Courses,
                onClick = onPopularCoursesClick
            )
            TextListTextButton(
                categories = uiState.categories,
                isLoading = uiState.isLoading,
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = onFilterCategorySelected
            )

            HorizontalCourseList(
                courses = uiState.courses,
                isLoading = uiState.isLoading,
                onCourseClick = onCourseClick
            )

            // 4. Top Mentors (Global)
            SectionHeader(
                titleId = R.string.top_Mentor,
                onClick = onTopMentorsListClick
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
                onClick = { onTabSelected(HomeTab.Explore) },
                modifier = Modifier.padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.start_exploring))
            }
        }
    }
}

@Composable
private fun SectionHeader(titleId: Int, onClick: () -> Unit) {
    SubTitle(
        text = titleId,
        onSubTitleButtonClick = onClick
    )
}

@Composable
private fun HorizontalCourseList(
    courses: List<Courses>,
    isLoading: Boolean,
    onCourseClick: (Courses) -> Unit
) {
    ShimmerBox(
        isLoading = isLoading,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        if (courses.isEmpty() && !isLoading) {
            EmptyStateText(
                text = stringResource(R.string.no_courses_available),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            val displayCourses = if (isLoading && courses.isEmpty()) {
                List(3) { Courses(id = "shimmer_$it", name = "Loading...") }
            } else courses

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = displayCourses,
                    key = { it.id }
                ) { course ->
                    CourseCard(
                        course = course,
                        onClick = { if (!isLoading) onCourseClick(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalMentorList(
    mentors: List<User.Mentor>,
    isLoading: Boolean,
    onMentorClick: (String) -> Unit
) {
    ShimmerBox(
        isLoading = isLoading,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        if (mentors.isEmpty() && !isLoading) {
            EmptyStateText(
                text = stringResource(R.string.no_mentors_found),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            val displayMentors = if (isLoading && mentors.isEmpty()) {
                List(3) { User.Mentor(userId = "shimmer_$it", fullName = "Loading...") }
            } else mentors

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = displayMentors,
                    key = { it.userId ?: it.fullName ?: it.hashCode() }
                ) { mentor ->
                    TeacherCard(
                        mentor = mentor,
                        onClick = {
                            if (!isLoading) {
                                mentor.userId?.let { onMentorClick(it) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 24.dp)
    )
}
