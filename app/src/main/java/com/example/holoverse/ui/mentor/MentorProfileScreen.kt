package com.example.holoverse.ui.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.reviews.domain.Review
import com.example.holoverse.ui.reviews.ui.ReviewViewModel
import com.example.holoverse.ui.reviews.ui.components.ReviewItem
import com.example.holoverse.ui.reviews.ui.components.WriteReviewDialog
import com.example.holoverse.ui.theme.IbarraNovaFont
import java.util.Locale

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
        ShimmerBox(
            isLoading = uiState.isLoading,
            baseColor = Color.DarkGray,
            durationMillis = 800
        ) {
            if (uiState.isLoading) {
                MentorProfileShimmer(modifier = Modifier.padding(paddingValues))
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: stringResource(R.string.unknown_error))
                }
            } else {
                uiState.mentor?.let { mentor ->
                    MentorProfileContent(
                        mentor = mentor,
                        courses = uiState.courses,
                        savedCourseIds = uiState.savedCourseIds,
                        savingCourseIds = uiState.savingCourseIds,
                        reviews = reviewState.reviews,
                        averageRating = reviewState.averageRating,
                        existingReview = existingReview,
                        isFollowing = uiState.isFollowing,
                        isUserLoggedIn = uiState.isUserLoggedIn,
                        isOwnProfile = uiState.isOwnProfile,
                        onFollowClick = { viewModel.toggleFollow() },
                        isFollowLoading = uiState.isFollowLoading,
                        onSaveCourseClick = { viewModel.toggleSaveCourse(it) },
                        onCourseClick = onCourseClick,
                        onMessageClick = onMessageClick,
                        onWriteReviewClick = { showReviewDialog = true },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
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
    savedCourseIds: List<String>,
    savingCourseIds: Set<String>,
    reviews: List<Review>,
    averageRating: Double,
    existingReview: Review?,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    isUserLoggedIn: Boolean,
    isOwnProfile: Boolean,
    onFollowClick: () -> Unit,
    onSaveCourseClick: (String) -> Unit,
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
                text = stringResource(mentor.specialization.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

//            mentor.hourlyRate?.let { rate ->
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = stringResource(R.string.price_format, rate) + "/hr",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.Bold
//                )
//            }

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
                    value = String.format(Locale.US, "%.1f", averageRating)
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
                        enabled = !isFollowLoading,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = null
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = if (isFollowing) stringResource(R.string.following) else stringResource(
                                    R.string.follow
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            items(
                items = courses,
                key = { it.id },
                contentType = { "mentor_course" }
            ) { course ->
                MentorCourseItem(
                    course = course,
                    isSaved = savedCourseIds.contains(course.id),
                    isSaving = savingCourseIds.contains(course.id),
                    onSaveClick = { onSaveCourseClick(course.id) },
                    onClick = { onCourseClick(course.id) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        } else {
            items(
                items = reviews,
                key = { it.id },
                contentType = { "mentor_review" }
            ) { review ->
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
fun MentorProfileShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(28.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Specialization
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp, 20.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(60.dp, 16.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bio Card content placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .background(
                    Color.Gray.copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        )
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
fun MentorCourseItem(
    course: Courses,
    isSaved: Boolean,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(course.category.titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                        contentDescription = "Save For Later",
                        modifier = Modifier.clickable { onSaveClick() },
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
