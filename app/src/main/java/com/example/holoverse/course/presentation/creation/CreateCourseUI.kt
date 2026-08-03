package com.example.holoverse.course.presentation.creation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.auth.presentation.common.widget.RadioButtonMenu
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.ui.theme.IbarraNovaFont
import com.example.holoverse.core.utils.Response
import com.example.holoverse.course.domain.AdCardStyle
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.CourseSession
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.Question
import com.example.holoverse.course.domain.Quiz
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onCourseCreated: () -> Unit,
    viewModel: CreateCourseViewModel = hiltViewModel(),
    darkTheme: Boolean = true,
) {
    val context = LocalContext.current
    val createCourseState by viewModel.createCourseState
    val uploadImageState by viewModel.uploadImageState
    val boostCourseState by viewModel.boostCourseState
    val lastCreatedCourse by viewModel.lastCreatedCourse
    val mentorCategory by viewModel.mentorCategory

    // State for course details
    var name by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // UI control states
    var showSessionDialog by remember { mutableStateOf(false) }
    var editingSessionIndex by remember { mutableStateOf<Int?>(null) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var editingQuizIndex by remember { mutableStateOf<Int?>(null) }
    var showBoostConfirmation by remember { mutableStateOf(false) }
    var showAdSelection by remember { mutableStateOf(false) }
    var showPlanSelection by remember { mutableStateOf(false) }
    var showPaymentSimulation by remember { mutableStateOf(false) }

    var selectedAdStyle by remember { mutableStateOf<AdCardStyle?>(null) }
    var selectedPlanDuration by remember { mutableStateOf("") }

    // Optimization: Remember expensive objects
    val backgroundBrush = remember(darkTheme) { Brush(darkTheme) }
    val levels = remember { listOf("Beginner", "Intermediate", "Advanced") }
    val languages = remember { listOf("English", "Arabic", "French", "Spanish", "Italian") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadImage(it) }
        }
    )

    // Side Effects
    LaunchedEffect(uploadImageState) {
        when (uploadImageState) {
            is Response.Success -> {
                imageUrl = (uploadImageState as Response.Success<String>).data
                Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
            is Response.Error -> {
                Toast.makeText(context, (uploadImageState as Response.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(createCourseState) {
        when (createCourseState) {
            is Response.Success -> {
                Toast.makeText(context, "Course created successfully!", Toast.LENGTH_SHORT).show()
                showBoostConfirmation = true
            }
            is Response.Error -> {
                Toast.makeText(context, (createCourseState as Response.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(boostCourseState) {
        if (boostCourseState is Response.Success) {
            Toast.makeText(context, "Boost applied!", Toast.LENGTH_SHORT).show()
            onCourseCreated()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundBrush),
        topBar = {
            CreateCourseTopBar(
                backgroundBrush = backgroundBrush,
                onBackClick = onCourseCreated
            )
        }
    ) { paddingValues ->
        // Dialogs extracted for clarity and scope control
        DialogContainer(
            showBoostConfirmation = showBoostConfirmation,
            showAdSelection = showAdSelection,
            showPlanSelection = showPlanSelection,
            showPaymentSimulation = showPaymentSimulation,
            lastCreatedCourse = lastCreatedCourse,
            onBoostConfirm = {
                showBoostConfirmation = false
                showAdSelection = true
            },
            onStyleSelected = { style ->
                selectedAdStyle = style
                showAdSelection = false
                showPlanSelection = true
            },
            onPlanSelected = { duration ->
                selectedPlanDuration = duration
                showPlanSelection = false
                showPaymentSimulation = true
            },
            onPaymentSuccess = {
                showPaymentSimulation = false
                lastCreatedCourse?.let { course ->
                    val durationDays = when (selectedPlanDuration) {
                        "7 Days" -> 7
                        "1 Month" -> 30
                        "3 Months" -> 90
                        else -> 7
                    }
                    val endTimestamp = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000L)
                    viewModel.boostCourse(
                        BoostedCourse(
                            courseId = course.id,
                            adCardStyle = selectedAdStyle ?: AdCardStyle.STYLE_1,
                            planDuration = selectedPlanDuration,
                            endTimestamp = endTimestamp,
                            courseName = course.name,
                            courseImageUrl = course.imageUrl,
                            courseDescription = course.description,
                            instructorName = course.instructorName
                        )
                    )
                }
            },
            onDismissBoost = {
                showBoostConfirmation = false
                onCourseCreated()
            },
            onDismissAdSelection = {
                showAdSelection = false
                onCourseCreated()
            },
            onDismissPlanSelection = {
                showPlanSelection = false
                onCourseCreated()
            }
        )

        if (showSessionDialog) {
            val sessionToEdit = remember(editingSessionIndex) {
                editingSessionIndex?.let { viewModel.sessions.getOrNull(it) }
            }
            SessionDialog(
                session = sessionToEdit,
                onDismiss = {
                    showSessionDialog = false
                    editingSessionIndex = null
                },
                onConfirm = { session ->
                    if (editingSessionIndex != null) {
                        viewModel.updateSession(editingSessionIndex!!, session)
                    } else {
                        viewModel.addSession(session)
                    }
                    showSessionDialog = false
                    editingSessionIndex = null
                }
            )
        }

        if (showQuizDialog) {
            val quizToEdit = remember(editingQuizIndex) {
                editingQuizIndex?.let { viewModel.quizzes.getOrNull(it) }
            }
            QuizDialog(
                quiz = quizToEdit,
                viewModel = viewModel,
                onDismiss = {
                    showQuizDialog = false
                    editingQuizIndex = null
                },
                onConfirm = { quiz ->
                    if (editingQuizIndex != null) {
                        viewModel.updateQuiz(editingQuizIndex!!, quiz)
                    } else {
                        viewModel.addQuiz(quiz)
                    }
                    showQuizDialog = false
                    editingQuizIndex = null
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CourseImagePicker(
                    imageUrl = imageUrl,
                    uploadImageState = uploadImageState,
                    onPickerLaunch = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            item {
                BasicInfoSection(
                    name = name,
                    onNameChange = { name = it },
                    mentorCategory = mentorCategory,
                    specialization = specialization,
                    onSpecializationChange = { specialization = it },
                    price = price,
                    onPriceChange = { price = it },
                    duration = duration,
                    onDurationChange = { duration = it },
                    level = level,
                    onLevelChange = { level = it },
                    levels = levels,
                    language = language,
                    onLanguageChange = { language = it },
                    languages = languages
                )
            }

            item {
                DescriptionSection(
                    description = description,
                    onDescriptionChange = { description = it }
                )
            }

            item {
                SyllabusHeader(
                    onAddSession = {
                        editingSessionIndex = null
                        showSessionDialog = true
                    }
                )
            }

            if (viewModel.sessions.isEmpty()) {
                item { EmptySyllabusPlaceholder() }
            }

            itemsIndexed(
                items = viewModel.sessions,
                key = { _, session -> session.title + session.date + session.time }
            ) { index, session ->
                SessionItem(
                    session = session,
                    onEdit = {
                        editingSessionIndex = index
                        showSessionDialog = true
                    },
                    onDelete = { viewModel.removeSession(index) }
                )
            }

            item {
                QuizHeader(
                    onAddQuiz = {
                        editingQuizIndex = null
                        showQuizDialog = true
                    }
                )
            }

            if (viewModel.quizzes.isEmpty()) {
                item { EmptyQuizPlaceholder() }
            }

            itemsIndexed(
                items = viewModel.quizzes,
                key = { _, quiz -> quiz.id.ifEmpty { quiz.title } }
            ) { index, quiz ->
                QuizItem(
                    quiz = quiz,
                    onEdit = {
                        editingQuizIndex = index
                        showQuizDialog = true
                    },
                    onDelete = { viewModel.removeQuiz(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                CreateCourseButton(
                    isLoading = createCourseState is Response.Loading,
                    onClick = {
                        if (name.isBlank() || price.isBlank() || imageUrl.isBlank() || level.isBlank() || language.isBlank() || specialization.isBlank()) {
                            val message = when {
                                imageUrl.isBlank() -> "Please upload a course image"
                                specialization.isBlank() -> "Please select a specialization"
                                level.isBlank() -> "Please select a course level"
                                language.isBlank() -> "Please select a course language"
                                else -> "Please fill required fields"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createCourse(
                                name = name,
                                specialization = specialization,
                                price = price,
                                duration = duration,
                                level = level,
                                language = language,
                                description = description,
                                imageUrl = imageUrl
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCourseTopBar(
    backgroundBrush: androidx.compose.ui.graphics.Brush,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(backgroundBrush)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            title = {
                Text(
                    "Create New Course",
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
        )
    }
}

@Composable
private fun CourseImagePicker(
    imageUrl: String,
    uploadImageState: Response<String>?,
    onPickerLaunch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onPickerLaunch),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Course Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = HoloCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap to upload course image",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (uploadImageState is Response.Loading) {
            CircularProgressIndicator(color = HoloCyan)
        }
    }
}

@Composable
private fun BasicInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    mentorCategory: AppCategory,
    specialization: String,
    onSpecializationChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    level: String,
    onLevelChange: (String) -> Unit,
    levels: List<String>,
    language: String,
    onLanguageChange: (String) -> Unit,
    languages: List<String>
) {
    var isSpecializationExpanded by remember { mutableStateOf(false) }
    var isLevelExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val specializationOptions = remember(mentorCategory) {
        mentorCategory.specializations.map { context.getString(it) }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HoloCyan
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            RadioButtonMenu(
                isExpanded = isSpecializationExpanded,
                onToggle = { isSpecializationExpanded = !isSpecializationExpanded },
                selectedItem = specialization.ifEmpty { "Select Specialization" },
                onItemSelected = { item ->
                    onSpecializationChange(item)
                    isSpecializationExpanded = false
                },
                menuItems = specializationOptions,
                showIcon = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = onDurationChange,
                    label = { Text("Duration") },
                    placeholder = { Text("e.g. 10 hrs") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            RadioButtonMenu(
                isExpanded = isLevelExpanded,
                onToggle = { isLevelExpanded = !isLevelExpanded },
                selectedItem = level.ifEmpty { "Select Level" },
                onItemSelected = { item ->
                    onLevelChange(item)
                    isLevelExpanded = false
                },
                menuItems = levels,
                showIcon = false
            )

            RadioButtonMenu(
                isExpanded = isLanguageExpanded,
                onToggle = { isLanguageExpanded = !isLanguageExpanded },
                selectedItem = language.ifEmpty { "Select Language" },
                onItemSelected = { item ->
                    onLanguageChange(item)
                    isLanguageExpanded = false
                },
                menuItems = languages,
                showIcon = false
            )
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HoloCyan
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Write about the course...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun SyllabusHeader(onAddSession: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Course Syllabus",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onAddSession,
            modifier = Modifier
                .clip(CircleShape)
                .background(HoloPurple)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Session",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EmptySyllabusPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No sessions added yet",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuizHeader(onAddQuiz: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Course Quizzes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onAddQuiz,
            modifier = Modifier
                .clip(CircleShape)
                .background(HoloCyan)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Quiz",
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmptyQuizPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No quizzes added yet",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuizItem(
    quiz: Quiz,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = HoloCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${quiz.questions.size} Questions • ${quiz.timeLimitMinutes} mins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SessionItem(
    session: CourseSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = HoloCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = HoloCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.date, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = HoloCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.time, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CreateCourseButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        CircularProgressIndicator(color = HoloPurple)
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
        ) {
            Text("Create Course", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DialogContainer(
    showBoostConfirmation: Boolean,
    showAdSelection: Boolean,
    showPlanSelection: Boolean,
    showPaymentSimulation: Boolean,
    lastCreatedCourse: com.example.holoverse.course.domain.Courses?,
    onBoostConfirm: () -> Unit,
    onStyleSelected: (AdCardStyle) -> Unit,
    onPlanSelected: (String) -> Unit,
    onPaymentSuccess: () -> Unit,
    onDismissBoost: () -> Unit,
    onDismissAdSelection: () -> Unit,
    onDismissPlanSelection: () -> Unit
) {
    if (showBoostConfirmation) {
        BoostConfirmationDialog(
            onDismiss = onDismissBoost,
            onConfirm = onBoostConfirm
        )
    }

    if (showAdSelection && lastCreatedCourse != null) {
        AdCardSelectionDialog(
            course = lastCreatedCourse,
            onDismiss = onDismissAdSelection,
            onStyleSelected = onStyleSelected
        )
    }

    if (showPlanSelection) {
        SubscriptionPlanDialog(
            onDismiss = onDismissPlanSelection,
            onPlanSelected = { duration, _ -> onPlanSelected(duration) }
        )
    }

    if (showPaymentSimulation) {
        PaymentSimulationDialog(onSuccess = onPaymentSuccess)
    }
}

@Composable
fun SessionDialog(
    session: CourseSession? = null,
    onDismiss: () -> Unit,
    onConfirm: (CourseSession) -> Unit
) {
    var title by remember { mutableStateOf(session?.title ?: "") }
    var date by remember { mutableStateOf(session?.date ?: "") }
    var time by remember { mutableStateOf(session?.time ?: "") }
    var description by remember { mutableStateOf(session?.description ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (session == null) "Add New Session" else "Edit Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HoloPurple
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        placeholder = { Text("HH:MM") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Session Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(CourseSession(title, date, time, description))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizDialog(
    quiz: Quiz? = null,
    viewModel: CreateCourseViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Quiz) -> Unit
) {
    var title by remember { mutableStateOf(quiz?.title ?: "") }
    var description by remember { mutableStateOf(quiz?.description ?: "") }
    var imageUrl by remember { mutableStateOf(quiz?.imageUrl ?: "") }
    var timeLimit by remember { mutableStateOf(quiz?.timeLimitMinutes?.toString() ?: "30") }
    val questions = remember { mutableStateListOf<Question>().apply { quiz?.questions?.let { addAll(it) } } }

    var showQuestionDialog by remember { mutableStateOf(false) }
    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    isUploading = true
                    val url = viewModel.uploadQuizImage(it)
                    if (url != null) {
                        imageUrl = url
                    }
                    isUploading = false
                }
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (quiz == null) "Create Quiz" else "Edit Quiz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HoloPurple
                )

                HoloImagePicker(
                    imageUrl = imageUrl,
                    onPickerLaunch = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    isLoading = isUploading
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = timeLimit,
                    onValueChange = { timeLimit = it },
                    label = { Text("Time Limit (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Quiz Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Questions (${questions.size})", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            editingQuestionIndex = null
                            showQuestionDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HoloCyan)
                    ) {
                        Text("Add Question", fontSize = 12.sp)
                    }
                }

                questions.forEachIndexed { index, question ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}. ${question.text}", modifier = Modifier.weight(1f), maxLines = 1)
                            IconButton(onClick = {
                                editingQuestionIndex = index
                                showQuestionDialog = true
                            }) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = HoloCyan)
                            }
                            IconButton(onClick = { questions.removeAt(index) }) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && questions.isNotEmpty()) {
                                onConfirm(
                                    Quiz(
                                        id = quiz?.id ?: UUID.randomUUID().toString(),
                                        title = title,
                                        description = description,
                                        timeLimitMinutes = timeLimit.toIntOrNull() ?: 30,
                                        questions = questions.toList(),
                                        imageUrl = imageUrl
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
                    ) {
                        Text("Save Quiz")
                    }
                }
            }
        }
    }

    if (showQuestionDialog) {
        QuestionDialog(
            question = editingQuestionIndex?.let { questions[it] },
            viewModel = viewModel,
            onDismiss = { showQuestionDialog = false },
            onConfirm = { question ->
                if (editingQuestionIndex != null) {
                    questions[editingQuestionIndex!!] = question
                } else {
                    questions.add(question)
                }
                showQuestionDialog = false
            }
        )
    }
}

@Composable
fun QuestionDialog(
    question: Question? = null,
    viewModel: CreateCourseViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Question) -> Unit
) {
    var text by remember { mutableStateOf(question?.text ?: "") }
    val options = remember { mutableStateListOf<String>().apply { 
        if (question != null) addAll(question.options) else repeat(4) { add("") }
    } }
    val correctIndices = remember { mutableStateListOf<Int>().apply { question?.correctOptionIndices?.let { addAll(it) } } }
    var imageUrl by remember { mutableStateOf(question?.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    isUploading = true
                    val url = viewModel.uploadQuizImage(it)
                    if (url != null) {
                        imageUrl = url
                    }
                    isUploading = false
                }
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add/Edit Question", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                HoloImagePicker(
                    imageUrl = imageUrl,
                    onPickerLaunch = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    isLoading = isUploading
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Options (Select correct ones):", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                options.forEachIndexed { index, option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = correctIndices.contains(index),
                            onCheckedChange = { checked ->
                                if (checked) correctIndices.add(index) else correctIndices.remove(index)
                            }
                        )
                        OutlinedTextField(
                            value = option,
                            onValueChange = { options[index] = it },
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (text.isNotBlank() && correctIndices.isNotEmpty() && options.all { it.isNotBlank() }) {
                                onConfirm(
                                    Question(
                                        id = question?.id ?: UUID.randomUUID().toString(),
                                        text = text,
                                        options = options.toList(),
                                        correctOptionIndices = correctIndices.toList(),
                                        imageUrl = imageUrl
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HoloCyan)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun HoloImagePicker(
    imageUrl: String,
    onPickerLaunch: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onPickerLaunch),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = HoloCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Upload Image",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(color = HoloCyan, modifier = Modifier.size(24.dp))
        }
    }
}
