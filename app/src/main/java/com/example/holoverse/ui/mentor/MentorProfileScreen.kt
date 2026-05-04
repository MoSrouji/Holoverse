package com.example.holoverse.ui.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.reviews.domain.Review
import com.example.holoverse.ui.reviews.ui.ReviewViewModel
import com.example.holoverse.ui.reviews.ui.components.ReviewItem
import com.example.holoverse.ui.reviews.ui.components.WriteReviewDialog
import com.example.holoverse.ui.home.component.CourseTypeWithButton
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.HoloverseTheme
import com.example.holoverse.ui.theme.IbarraNovaFont

@Composable
fun MentorProfileScreen(
    mentorId: String,
    onBackClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    viewModel: MentorProfileViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel(),
    darkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewState by reviewViewModel.uiState.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    val currentUser = reviewState.currentUser
    val existingReview = reviewState.reviews.find { it.userId == currentUser?.userId }

    LaunchedEffect(mentorId) {
        viewModel.loadMentorProfile(mentorId)
        reviewViewModel.loadReviews(mentorId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Mentor Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = IbarraNovaFont,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error ?: stringResource(R.string.unknown_error))
            }
        } else {
            uiState.mentor?.let { mentor ->
                MentorProfileContent(
                    mentor = mentor,
                    courses = uiState.courses,
                    reviews = reviewState.reviews,
                    existingReview = existingReview,
                    isFollowing = uiState.isFollowing,
                    isUserLoggedIn = uiState.isUserLoggedIn,
                    isOwnProfile = uiState.isOwnProfile,
                    onFollowClick = { viewModel.toggleFollow() },
                    onCourseClick = onCourseClick,
                    onMessageClick = onMessageClick,
                    onWriteReviewClick = { showReviewDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        if (showReviewDialog) {
            WriteReviewDialog(
                onDismiss = { showReviewDialog = false },
                onSubmit = { rating, comment ->
                    if (currentUser != null) {
                        val review = Review(
                            targetId = mentorId,
                            userId = currentUser.userId ?: "",
                            userName = currentUser.fullName ?: "Anonymous User",
                            userImageUrl = when (currentUser) {
                                is User.Student -> currentUser.profileImageUrl ?: ""
                                is User.Mentor -> currentUser.profileImageUrl ?: ""
                            },
                            rating = rating,
                            comment = comment
                        )
                        reviewViewModel.submitReview(review)
                        showReviewDialog = false
                    }
                },
                isSubmitting = reviewState.isSubmitting,
                initialRating = existingReview?.rating ?: 0f,
                initialComment = existingReview?.comment ?: ""
            )
        }
    }
}

@Composable
fun MentorProfileContent(
    mentor: User.Mentor,
    courses: List<Courses>,
    reviews: List<Review>,
    existingReview: Review?,
    isFollowing: Boolean,
    isUserLoggedIn: Boolean,
    isOwnProfile: Boolean,
    onFollowClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.courses), stringResource(R.string.ratings))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AsyncImage(
                model = mentor.profileImageUrl,
                contentDescription = mentor.fullName,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                error = painterResource(R.drawable.istockphoto_1934800957_612x612)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mentor.fullName ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${
                    mentor.specialization.name.lowercase().replaceFirstChar { it.uppercase() }
                }${stringResource(R.string.at_google)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.courses),
                    value = (mentor.coursesCreated?.size ?: 0).toString()
                )
                StatItem(
                    label = stringResource(R.string.students),
                    value = formatValue(mentor.followersCount ?: 0)
                )
                StatItem(
                    label = stringResource(R.string.following),
                    value = formatValue(mentor.followingCount ?: 0)
                )
                StatItem(
                    label = stringResource(R.string.ratings),
                    value = String.format(Locale.US, "%.1f", mentor.averageRating ?: 0.0)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isUserLoggedIn && !isOwnProfile) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = if (isFollowing) stringResource(R.string.following) else stringResource(
                                R.string.follow
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onMessageClick(mentor.userId ?: "") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.message), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = mentor.bio ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SecondaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(selectedTab),
                                color = Color.Transparent
                            )
                        },
                        divider = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            ) {
                                Text(
                                    text = title,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 1) {
                        Button(
                            onClick = onWriteReviewClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (existingReview != null) "Edit Your Review" else "Write a Review")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (selectedTab == 0) {
            items(courses) { course ->
                MentorCourseItem(course = course, onClick = { onCourseClick(course.id) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        } else {
            items(reviews) { review ->
                ReviewItem(
                    review = review,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reviews yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MentorCourseItem(course: Courses, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = course.imageUrl,
            contentDescription = course.name,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            CourseTypeWithButton(category = course.category)

            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.price_format, course.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.price_format, course.price * 1.2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107), // Keeping gold for star
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = " ${course.rating}  |  ${
                        stringResource(
                            R.string.enrolled_count,
                            formatValue(course.numEnrolled)
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


private fun formatValue(num: Int): String {
    return if (num >= 1000) {
        String.format(Locale.US, "%.1fk", num / 1000.0)
    } else {
        num.toString()
    }
}
