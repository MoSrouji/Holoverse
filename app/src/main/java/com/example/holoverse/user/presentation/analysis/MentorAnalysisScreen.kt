package com.example.holoverse.user.presentation.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.course.domain.Courses
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorAnalysisScreen(
    onBackClick: () -> Unit,
    darkTheme: Boolean,
    viewModel: MentorAnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mentor Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadData() }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text(
                            "Overall Performance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        MentorStatsGrid(uiState)
                    }

                    item {
                        Text(
                            "Course Performance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.courses) { course ->
                        CourseAnalysisCard(course)
                    }
                }
            }
        }
    }
}

@Composable
fun MentorStatsGrid(uiState: MentorAnalysisUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Students",
                value = uiState.totalStudents.toString(),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                color = Color(0xFF42A5F5)
            )
            StatCard(
                title = "Total Followers",
                value = uiState.totalFollowers.toString(),
                icon = Icons.Default.PersonAdd,
                modifier = Modifier.weight(1f),
                color = Color(0xFFAB47BC)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Avg Rating",
                value = String.format(Locale.getDefault(), "%.2f", uiState.averageRating),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFCA28)
            )
            StatCard(
                title = "Total Revenue",
                value = String.format(Locale.getDefault(), "$%.0f", uiState.totalRevenue),
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f),
                color = Color(0xFFEC407A)
            )
        }
        uiState.mentor?.hourlyRate?.let { rate ->
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Hourly Rate",
                    value = String.format(Locale.getDefault(), "$%.2f/hr", rate),
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF66BB6A)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CourseAnalysisCard(course: Courses) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CourseMetric("Enrolled", course.numEnrolled.toString())
                CourseMetric(
                    "Completion",
                    String.format(Locale.getDefault(), "%.0f%%", course.completionRate * 100)
                )
                CourseMetric(
                    "Avg Progress",
                    String.format(Locale.getDefault(), "%.0f%%", course.averageProgress * 100)
                )
                CourseMetric(
                    "Revenue",
                    String.format(Locale.getDefault(), "$%.0f", course.price * course.numEnrolled)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show Enrollment breakdown hint
            Text(
                text = "Total Enrollments: ${course.numEnrolled}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visual Representation - Simple Bar Chart
            CoursePerformanceChart(
                completion = course.completionRate.toFloat(),
                progress = course.averageProgress.toFloat()
            )
        }
    }
}

@Composable
fun CourseMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CoursePerformanceChart(completion: Float, progress: Float) {
    val barColor1 = Color(0xFF4FC3F7)
    val barColor2 = Color(0xFF9575CD)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(12.dp)
                .background(barColor1, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Completion Rate", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier
                .size(12.dp)
                .background(barColor2, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Avg Progress", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)) {
            val width = size.width
            val barHeight = 12.dp.toPx()

            // Completion Bar
            drawRect(
                color = Color.LightGray.copy(alpha = 0.3f),
                topLeft = Offset(0f, 0f),
                size = Size(width, barHeight)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(barColor1.copy(alpha = 0.7f), barColor1)),
                topLeft = Offset(0f, 0f),
                size = Size(width * completion, barHeight)
            )

            // Progress Bar
            drawRect(
                color = Color.LightGray.copy(alpha = 0.3f),
                topLeft = Offset(0f, barHeight + 8.dp.toPx()),
                size = Size(width, barHeight)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(barColor2.copy(alpha = 0.7f), barColor2)),
                topLeft = Offset(0f, barHeight + 8.dp.toPx()),
                size = Size(width * progress, barHeight)
            )
        }
    }
}


