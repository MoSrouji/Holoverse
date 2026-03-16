package com.example.holoverse.ui.courseDetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import com.example.holoverse.utils.Response

data class CourseSession(
    val title: String,
    val date: String,
    val time: String
)

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onApplyClick: (String) -> Unit
) {
    val courseState by viewModel.courseState

    when (courseState) {
        is Response.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HoloCyan)
            }
        }
        is Response.Success -> {
            val course = (courseState as Response.Success<Courses?>).data
            if (course != null) {
                CourseDetailContent(
                    course = course,
                    onBackClick = onBackClick,
                    onApplyClick = { onApplyClick(course.id) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Course not found", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        is Response.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (courseState as Response.Error).massage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailContent(
    course: Courses,
    onBackClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val sessions = listOf(
        CourseSession("Introduction", "7/2/2026", "10:30 -> 11:30"),
        CourseSession("Fundamentals of Design", "9/2/2025", "12:00 -> 14:20"),
        CourseSession("Advanced Techniques", "11/2/2026", "13:00 -> 15:00"),
        CourseSession("Project Presentation", "11/2/2026", "13:00 -> 15:00")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Course Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
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
                            text = "$${course.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = HoloCyan
                        )
                    }
                    Button(
                        onClick = onApplyClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HoloPurple,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(180.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Enroll Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                        listOf(HoloPurple.copy(alpha = 0.7f), HoloCyan.copy(alpha = 0.7f))
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
                            text = " ${course.rating} (${course.numReviews} reviews)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Info Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(Icons.Default.AccessTime, course.duration)
                        InfoChip(Icons.Default.Layers, course.level)
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
                        text = if (course.description.isNotEmpty()) course.description else "No description available for this course yet. Stay tuned for updates!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Course Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Syllabus / Timeline
            itemsIndexed(sessions) { index, session ->
                TimelineItem(
                    session = session,
                    isFirst = index == 0,
                    isLast = index == sessions.size - 1
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = HoloCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun TimelineItem(
    session: CourseSession,
    isFirst: Boolean,
    isLast: Boolean
) {
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
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
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

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    MaterialTheme {
        CourseDetailContent(
            course = Courses(
                name = "Advanced UI/UX Masterclass",
                description = "Master the art of creating stunning user interfaces and seamless user experiences. This course covers everything from wireframing to high-fidelity prototyping using industry-standard tools.",
                price = 49.99,
                duration = "12 Hours",
                level = "Advanced",
                rating = 4.8,
                numReviews = 124
            ),
            onBackClick = {},
            onApplyClick = {}
        )
    }
}
