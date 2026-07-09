package com.example.holoverse.admin.presentation.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.admin.domain.repository.Timeframe
import com.example.holoverse.admin.presentation.viewmodel.AdminUiState
import com.example.holoverse.admin.presentation.viewmodel.AdminViewModel
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.theme.HoloverseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlPanelScreen(
    onBackClick: () -> Unit,
    darkTheme: Boolean,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    AdminControlPanelContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTimeframeSelected = viewModel::onTimeframeSelected,
        darkTheme = darkTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlPanelContent(
    uiState: AdminUiState,
    onBackClick: () -> Unit,
    onTimeframeSelected: (Timeframe) -> Unit,
    darkTheme: Boolean
) {
    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.admin_control_panel), 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = if (darkTheme) Color.White else Color.Black
                ),
                modifier = Modifier.background(headerBrush)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        AdminSummarySection(uiState)
                    }

                    item {
                        UserGrowthSection(uiState, onTimeframeSelected)
                    }

                    item {
                        ChartSection(uiState)
                    }

                    item {
                        ActivitySection(uiState)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSummarySection(uiState: AdminUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.platform_overview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = stringResource(R.string.total_students),
                value = uiState.studentCount.toString(),
                icon = Icons.Default.Groups,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = stringResource(R.string.total_mentors),
                value = uiState.mentorCount.toString(),
                icon = Icons.Default.Person,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = stringResource(R.string.active_courses),
                value = uiState.courseCount.toString(),
                icon = Icons.Default.Book,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = stringResource(R.string.total_revenue),
                value = "$${uiState.totalRevenue.toLong()}",
                icon = Icons.Default.MonetizationOn,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGrowthSection(uiState: AdminUiState, onTimeframeSelected: (Timeframe) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "User Growth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Timeframe.entries.forEachIndexed { index, timeframe ->
                    SegmentedButton(
                        selected = uiState.selectedTimeframe == timeframe,
                        onClick = { onTimeframeSelected(timeframe) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = Timeframe.entries.size)
                    ) {
                        Text(timeframe.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.userGrowthData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No growth data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                UserGrowthLinearChart(
                    data = uiState.userGrowthData,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
        }
    }
}

@Composable
fun UserGrowthLinearChart(data: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    val maxVal = remember(data) { data.maxOfOrNull { it.second }?.toFloat() ?: 1f }
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val spacingX = width / (data.size - 1).coerceAtLeast(1)
            
            // Draw Grid Lines (Horizontal)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (i * (height / gridLines))
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw Line
            val path = Path()
            data.forEachIndexed { index, pair ->
                val x = index * spacingX
                val y = height - (pair.second / maxVal * height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw Points
            data.forEachIndexed { index, pair ->
                val x = index * spacingX
                val y = height - (pair.second / maxVal * height)
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEachIndexed { index, pair ->
                // Show only a few labels if there are many
                if (data.size <= 7 || index % (data.size / 4) == 0 || index == data.size - 1) {
                    Text(
                        pair.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChartSection(uiState: AdminUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.course_categories_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulated Donut Chart
                DonutChart(
                    data = uiState.categoryDistribution,
                    modifier = Modifier.size(150.dp)
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = listOf(Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFE91E63))
                    uiState.categoryDistribution.entries.take(5).forEachIndexed { index, entry ->
                        LegendItem(
                            label = entry.key,
                            value = entry.value.toString(),
                            color = colors[index % colors.size]
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(data: Map<String, Int>, modifier: Modifier = Modifier) {
    val total = data.values.sum().toFloat()
    val colors = listOf(Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFE91E63))
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40f)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ($value)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ActivitySection(uiState: AdminUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.recent_activity_highlights),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column {
                ActivityItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = stringResource(R.string.weekly_user_growth),
                    subtitle = stringResource(R.string.new_signups_this_week, uiState.userGrowthData.sumOf { it.second }),
                    color = Color(0xFF4CAF50)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                ActivityItem(
                    icon = Icons.Default.Analytics,
                    title = stringResource(R.string.platform_engagement),
                    subtitle = stringResource(R.string.high_activity_message, "Development"),
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun ActivityItem(icon: ImageVector, title: String, subtitle: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminControlPanelPreview() {
    HoloverseTheme(darkTheme = true) {
        AdminControlPanelContent(
            uiState = AdminUiState(
                studentCount = 120,
                mentorCount = 45,
                courseCount = 32,
                totalRevenue = 5400.0,
                userGrowthData = listOf(
                    "Mon" to 5,
                    "Tue" to 8,
                    "Wed" to 4,
                    "Thu" to 12,
                    "Fri" to 7,
                    "Sat" to 15,
                    "Sun" to 9
                ),
                categoryDistribution = mapOf(
                    "Development" to 15,
                    "Design" to 10,
                    "Business" to 7
                ),
                selectedTimeframe = Timeframe.WEEK
            ),
            onBackClick = {},
            onTimeframeSelected = {},
            darkTheme = true
        )
    }
}
