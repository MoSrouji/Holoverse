package com.example.holoverse.course.presentation.creation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.ui.theme.IbarraNovaFont
import com.example.holoverse.core.utils.Response
import com.example.holoverse.course.domain.Courses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseScreen(
    courseId: String,
    onCourseUpdated: () -> Unit,
    viewModel: EditCourseViewModel = hiltViewModel(),
    darkTheme: Boolean = true,
) {
    val context = LocalContext.current
    val editCourseState by viewModel.editCourseState
    val courseLoadState by viewModel.courseLoadState
    val uploadImageState by viewModel.uploadImageState
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
    val selectedTimeSlots = remember { mutableStateListOf<String>() }

    // UI control states
    var showSessionDialog by remember { mutableStateOf(false) }
    var editingSessionIndex by remember { mutableStateOf<Int?>(null) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var editingQuizIndex by remember { mutableStateOf<Int?>(null) }

    val backgroundBrush = remember(darkTheme) { Brush(darkTheme) }
    val levels = remember { listOf("Beginner", "Intermediate", "Advanced") }
    val languages = remember { listOf("English", "Arabic", "French", "Spanish", "Italian") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadImage(it) }
        }
    )

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    LaunchedEffect(courseLoadState) {
        if (courseLoadState is Response.Success) {
            val course = (courseLoadState as Response.Success<Courses?>).data
            if (course != null) {
                name = course.name
                specialization = course.specialization
                price = course.price.toString()
                duration = course.duration
                level = course.level
                language = course.language
                description = course.description
                imageUrl = course.imageUrl
                selectedTimeSlots.clear()
                selectedTimeSlots.addAll(course.availableTimeSlots)
            }
        }
    }

    LaunchedEffect(uploadImageState) {
        if (uploadImageState is Response.Success) {
            imageUrl = (uploadImageState as Response.Success<String>).data
            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
        } else if (uploadImageState is Response.Error) {
            Toast.makeText(context, (uploadImageState as Response.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(editCourseState) {
        if (editCourseState is Response.Success) {
            Toast.makeText(context, "Course updated successfully!", Toast.LENGTH_SHORT).show()
            onCourseUpdated()
        } else if (editCourseState is Response.Error) {
            Toast.makeText(context, (editCourseState as Response.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
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
                            "Edit Course",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = IbarraNovaFont,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCourseUpdated) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (courseLoadState is Response.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = HoloPurple)
            } else if (courseLoadState is Response.Error) {
                Text(
                    (courseLoadState as Response.Error).message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
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
                    val createCourseViewModel: CreateCourseViewModel = hiltViewModel() // Temporarily for reuse
                    QuizDialog(
                        quiz = quizToEdit,
                        viewModel = createCourseViewModel, // We can reuse the upload logic if needed or fix it
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
                        TimeSlotSection(
                            availableSlots = levels,
                            selectedSlots = selectedTimeSlots,
                            onToggleSlot = { slot ->
                                if (selectedTimeSlots.contains(slot)) {
                                    selectedTimeSlots.remove(slot)
                                } else {
                                    selectedTimeSlots.add(slot)
                                }
                            }
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
                        if (editCourseState is Response.Loading) {
                            CircularProgressIndicator(color = HoloPurple)
                        } else {
                            Button(
                                onClick = {
                                    if (name.isBlank() || price.isBlank() || imageUrl.isBlank() || level.isBlank() || language.isBlank() || specialization.isBlank() || selectedTimeSlots.isEmpty()) {
                                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateCourse(
                                            courseId = courseId,
                                            name = name,
                                            specialization = specialization,
                                            price = price,
                                            duration = duration,
                                            level = level,
                                            language = language,
                                            description = description,
                                            imageUrl = imageUrl,
                                            availableTimeSlots = selectedTimeSlots.toList()
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
                            ) {
                                Text("Update Course", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
