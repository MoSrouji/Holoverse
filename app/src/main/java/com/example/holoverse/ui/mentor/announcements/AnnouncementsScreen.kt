package com.example.holoverse.ui.mentor.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.courses.domain.AdCardStyle
import com.example.holoverse.courses.domain.BoostedCourse
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.teacherpart.courses.*
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.utils.Response
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    onBackClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean,
    viewModel: AnnouncementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedCourseToBoost by remember { mutableStateOf<Courses?>(null) }
    var showBoostConfirmation by remember { mutableStateOf(false) }
    var showAdSelection by remember { mutableStateOf(false) }
    var showPlanSelection by remember { mutableStateOf(false) }
    var showPaymentSimulation by remember { mutableStateOf(false) }

    var selectedStyle by remember { mutableStateOf(AdCardStyle.STYLE_1) }
    var selectedPlanDuration by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.announcements),
                        fontFamily = IbarraNovaFont,
                        fontWeight = FontWeight.Bold
                    )
                },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                ShimmerBox(
                    isLoading = true,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnnouncementsShimmer()
                }
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "Unknown Error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (uiState.courses.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No courses found to boost",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.courses) { course ->
                        val boostedInfo = uiState.boostedCourses.find { it.courseId == course.id }
                        MentorCourseItem(
                            course = course,
                            boostedCourse = boostedInfo,
                            onBoostClick = {
                                selectedCourseToBoost = course
                                showBoostConfirmation = true
                            }
                        )
                    }
                }
            }

            // Boost Flow Dialogs
            if (showBoostConfirmation) {
                BoostConfirmationDialog(
                    onDismiss = { showBoostConfirmation = false },
                    onConfirm = {
                        showBoostConfirmation = false
                        showAdSelection = true
                    }
                )
            }

            if (showAdSelection && selectedCourseToBoost != null) {
                AdCardSelectionDialog(
                    course = selectedCourseToBoost!!,
                    onDismiss = { showAdSelection = false },
                    onStyleSelected = { style ->
                        selectedStyle = style
                        showAdSelection = false
                        showPlanSelection = true
                    }
                )
            }

            if (showPlanSelection) {
                SubscriptionPlanDialog(
                    onDismiss = { showPlanSelection = false },
                    onPlanSelected = { duration, _ ->
                        selectedPlanDuration = duration
                        showPlanSelection = false
                        showPaymentSimulation = true
                    }
                )
            }

            if (showPaymentSimulation && selectedCourseToBoost != null) {
                PaymentSimulationDialog(
                    onSuccess = {
                        showPaymentSimulation = false
                        val durationDays = when (selectedPlanDuration) {
                            "7 Days" -> 7L
                            "1 Month" -> 30L
                            "3 Months" -> 90L
                            else -> 7L
                        }
                        val endTimestamp = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(durationDays)
                        
                        val boostedCourse = BoostedCourse(
                            courseId = selectedCourseToBoost!!.id,
                            adCardStyle = selectedStyle,
                            planDuration = selectedPlanDuration,
                            endTimestamp = endTimestamp,
                            courseName = selectedCourseToBoost!!.name,
                            courseImageUrl = selectedCourseToBoost!!.imageUrl,
                            courseDescription = selectedCourseToBoost!!.description,
                            instructorName = selectedCourseToBoost!!.instructorName
                        )
                        viewModel.boostCourse(boostedCourse)
                    }
                )
            }

            // Handle Boost Response
            LaunchedEffect(uiState.boostResponse) {
                if (uiState.boostResponse is Response.Success) {
                    viewModel.resetBoostResponse()
                }
            }
        }
    }
}

@Composable
fun AnnouncementsShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(6) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(20.dp)
                                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(16.dp)
                                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

@Composable
fun MentorCourseItem(
    course: Courses,
    boostedCourse: BoostedCourse?,
    onBoostClick: () -> Unit
) {
    val isBoosted = boostedCourse != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isBoosted) HoloCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course Image
            AsyncImage(
                model = course.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                if (isBoosted) {
                    BoostedStatus(boostedCourse)
                } else {
                    Text(
                        text = "Not Boosted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (!isBoosted) {
                IconButton(
                    onClick = onBoostClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(HoloPurple.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.RocketLaunch,
                        contentDescription = "Boost",
                        tint = HoloPurple
                    )
                }
            }
        }
    }
}

@Composable
fun BoostedStatus(boostedCourse: BoostedCourse) {
    val remainingMillis = boostedCourse.endTimestamp - System.currentTimeMillis()
    val remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis)
    val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24

    val timeText = if (remainingMillis <= 0) {
        "Expired"
    } else if (remainingDays > 0) {
        "$remainingDays d $remainingHours h left"
    } else {
        "$remainingHours h left"
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = HoloCyan.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "BOOSTED",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HoloCyan,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = HoloCyan
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = HoloCyan,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
