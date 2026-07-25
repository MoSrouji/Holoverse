package com.example.holoverse.course.presentation.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.ui.theme.HoloverseTheme
import com.example.holoverse.core.ui.theme.IbarraNovaFont
import com.example.holoverse.core.utils.Response
import com.example.holoverse.course.domain.CourseSession
import com.example.holoverse.course.domain.Courses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onInstructorClick: (String) -> Unit,
    onEnrollSuccess: () -> Unit,
    darkTheme: Boolean = true
) {
    LaunchedEffect(courseId) {
        viewModel.initialize(courseId)
    }

    val courseState by viewModel.courseState
    val instructorState by viewModel.instructorState
    val enrollmentState by viewModel.enrollmentState
    val saveStatus by viewModel.saveStatus
    val isEnrolled by viewModel.isEnrolled
    val isSaved by viewModel.isSaved
    val context = LocalContext.current
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(enrollmentState) {
        when (enrollmentState) {
            is Response.Success -> {
                Toast.makeText(context, "Successfully Enrolled!", Toast.LENGTH_SHORT).show()
                onEnrollSuccess()
                viewModel.resetEnrollmentState()
            }

            is Response.Error -> {
                Toast.makeText(
                    context,
                    (enrollmentState as Response.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetEnrollmentState()
            }

            else -> {}
        }
    }

    LaunchedEffect(saveStatus) {
        when (saveStatus) {
            is Response.Success -> {
                val message = if (isSaved) "Added to Save for Later" else "Removed from Saved"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveStatus()
            }

            is Response.Error -> {
                Toast.makeText(context, (saveStatus as Response.Error).message, Toast.LENGTH_SHORT)
                    .show()
                viewModel.resetSaveStatus()
            }

            else -> {}
        }
    }

    if (showPaymentDialog && courseState is Response.Success) {
        val course = (courseState as Response.Success<Courses?>).data
        if (course != null) {
            PaymentConfirmationDialog(
                course = course,
                onConfirm = {
                    showPaymentDialog = false
                    viewModel.enrollInCourse(course.id)
                },
                onDismiss = { showPaymentDialog = false }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))

            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Course Details",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = IbarraNovaFont,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            (courseState as? Response.Success)?.data?.id?.let {
                                viewModel.toggleSaveCourse(it)
                            }
                        }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Course",
                                tint = if (isSaved) HoloCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                )
            }
        },
        bottomBar = {
            if (courseState is Response.Success) {
                val course = (courseState as Response.Success<Courses?>).data
                if (course != null) {
                    CourseDetailBottomBar(
                        course = course,
                        enrollmentState = enrollmentState,
                        isEnrolled = isEnrolled,
                        onEnrollClick = { showPaymentDialog = true }
                    )
                }
            } else if (courseState is Response.Loading) {
                ShimmerBox(
                    isLoading = true,
                    baseColor = Color.DarkGray,
                    durationMillis = 800
                ) {
                    CourseDetailBottomBarShimmer()
                }
            }
        }
    ) { paddingValues ->
        ShimmerBox(
            isLoading = courseState is Response.Loading,
            baseColor = Color.DarkGray,
            durationMillis = 800
        ) {
            when (courseState) {
                is Response.Loading -> {
                    CourseDetailShimmer(modifier = Modifier.padding(paddingValues))
                }

                is Response.Success -> {
                    val course = (courseState as Response.Success<Courses?>).data
                    val instructor = (instructorState as? Response.Success)?.data
                    if (course != null) {
                        CourseDetailContent(
                            course = course,
                            instructor = instructor,
                            onInstructorClick = onInstructorClick,
                            darkTheme = darkTheme,
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Course not found",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                is Response.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (courseState as Response.Error).toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

enum class CourseDetailTab {
    Courses, Ratings
}

@Composable
fun CourseDetailContent(
    course: Courses,
    instructor: User.Mentor?,
    onInstructorClick: (String) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CourseDetailTab.Courses) }

    val sessions = course.sessions.ifEmpty {
        listOf(
            CourseSession(
                "Introduction",
                "7/2/2026",
                "10:30 -> 11:30",
                "In this session, we will dive deep into the core concepts and practical applications of the topic. Expect hands-on exercises and expert insights."
            ),
            CourseSession(
                "Fundamentals of Design",
                "9/2/2025",
                "12:00 -> 14:20",
                "In this session, we will dive deep into the core concepts and practical applications of the topic. Expect hands-on exercises and expert insights."
            ),
            CourseSession(
                "Advanced Techniques",
                "11/2/2026",
                "13:00 -> 15:00",
                "In this session, we will dive deep into the core concepts and practical applications of the topic. Expect hands-on exercises and expert insights."
            ),
            CourseSession(
                "Project Presentation",
                "11/2/2026",
                "13:00 -> 15:00",
                "In this session, we will dive deep into the core concepts and practical applications of the topic. Expect hands-on exercises and expert insights."
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            // Course Image with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                if (course.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = "Course Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        HoloPurple.copy(alpha = 0.7f),
                                        HoloCyan.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = course.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = " ${"%.2f".format(course.rating)} (${course.numReviews} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Instructor Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .clickable {
                            val id = instructor?.userId ?: course.instructorId
                            if (id.isNotEmpty()) {
                                onInstructorClick(id)
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = instructor?.profileImageUrl,
                        contentDescription = "Instructor Image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = instructor?.fullName
                                ?: course.instructorName.ifEmpty { "Instructor" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = instructor?.specialization?.name?.lowercase()
                                ?.replaceFirstChar { it.uppercase() } ?: "Professional Instructor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(Icons.Default.AccessTime, course.duration)
                    InfoChip(Icons.Default.Layers, course.level)
                    InfoChip(Icons.Default.Group, "${course.numEnrolled} Students")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = course.description.ifEmpty { "No description available for this course yet. Stay tuned for updates!" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                CourseDetailToggle(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (selectedTab == CourseDetailTab.Courses) {
            // Syllabus / Timeline
            itemsIndexed(
                items = sessions,
                key = { _, session -> session.title },
                contentType = { _, _ -> "timeline_item" }
            ) { index, session ->
                TimelineItem(
                    session = session,
                    isFirst = index == 0,
                    isLast = index == sessions.size - 1
                )
            }
        } else {
            item(
                key = "rating_section",
                contentType = "rating_section"
            ) {
                RatingSection(course)
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun CourseDetailToggle(
    selectedTab: CourseDetailTab,
    onTabSelected: (CourseDetailTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabItem(
                title = "Content",
                isSelected = selectedTab == CourseDetailTab.Courses,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(CourseDetailTab.Courses) }
            )
            TabItem(
                title = "Ratings",
                isSelected = selectedTab == CourseDetailTab.Ratings,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(CourseDetailTab.Ratings) }
            )
        }
    }
}

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "tabBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun RatingSection(course: Courses) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "%.2f".format(course.rating),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < course.rating.toInt()) Color(0xFFFFC107) else Color.Gray.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${course.numReviews} Reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Simplified Rating Bars
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 32.dp)
            ) {
                RatingBar(5, 0.8f)
                RatingBar(4, 0.15f)
                RatingBar(3, 0.03f)
                RatingBar(2, 0.01f)
                RatingBar(1, 0.01f)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Placeholder for individual reviews
        Text(
            text = "Latest Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Example Review
        ReviewItem(
            name = "Alex Johnson",
            rating = 5,
            comment = "This course exceeded my expectations! The content is very well structured and easy to follow."
        )
    }
}

@Composable
fun RatingBar(stars: Int, percentage: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$stars",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = HoloCyan,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ReviewItem(name: String, rating: Int, comment: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.2f
            )
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Row {
                    repeat(rating) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = HoloCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun TimelineItem(
    session: CourseSession,
    isFirst: Boolean,
    isLast: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Line and Circle
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val circleRadius = 8.dp.toPx()
                val strokeWidth = 3.dp.toPx()
                val circleCenterY = 24.dp.toPx()

                // Draw vertical line
                val startY = if (isFirst) circleCenterY else 0f
                val endY = if (isLast) circleCenterY else size.height

                drawLine(
                    color = HoloCyan.copy(alpha = 0.5f),
                    start = Offset(centerX, startY),
                    end = Offset(centerX, endY),
                    strokeWidth = strokeWidth
                )

                // Draw circle
                drawCircle(
                    color = Color.White,
                    radius = circleRadius,
                    center = Offset(centerX, circleCenterY)
                )
                drawCircle(
                    color = HoloCyan,
                    radius = circleRadius,
                    center = Offset(centerX, circleCenterY),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = HoloPurple,
                    radius = 3.dp.toPx(),
                    center = Offset(centerX, circleCenterY)
                )
            }
        }

        // Session Content Card
        Card(
            modifier = Modifier
                .padding(start = 16.dp, bottom = 24.dp)
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = HoloCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = session.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = HoloCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = session.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = HoloCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = session.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentConfirmationDialog(
    course: Courses,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Enrollment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You are about to enroll in:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HoloCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${"%.2f".format(course.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = HoloPurple
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HoloPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Pay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    HoloverseTheme {
        CourseDetailContent(
            course = Courses(
                name = "Advanced UI/UX Masterclass",
                specialization = "UI/UX Design",
                description = "Master the art of creating stunning user interfaces and seamless user experiences. This course covers everything from wireframing to high-fidelity prototyping using industry-standard tools.",
                price = 49.99,
                duration = "12 Hours",
                level = "Advanced",
                rating = 4.8,
                numReviews = 124,
                instructorName = "John Doe"
            ),
            instructor = null,
            onInstructorClick = {},
            darkTheme = true
        )
    }
}

@Composable
fun CourseDetailShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(32.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(36.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .padding(vertical = 4.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun CourseDetailBottomBarShimmer() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(40.dp, 12.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp, 24.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
            }
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun CourseDetailBottomBar(
    course: Courses,
    enrollmentState: Response<Boolean>?,
    isEnrolled: Boolean,
    onEnrollClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${"%.2f".format(course.price)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HoloCyan
                )
            }
            Button(
                onClick = onEnrollClick,
                enabled = !isEnrolled && enrollmentState !is Response.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnrolled) MaterialTheme.colorScheme.secondaryContainer else HoloPurple,
                    contentColor = if (isEnrolled) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                    disabledContainerColor = if (isEnrolled) MaterialTheme.colorScheme.secondaryContainer else HoloPurple.copy(
                        alpha = 0.6f
                    )
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (enrollmentState is Response.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEnrolled) "Enrolled" else "Enroll Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


