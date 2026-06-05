package com.example.holoverse.ui.teacherPart.courses

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.AdCardStyle
import com.example.holoverse.courses.domain.BoostedCourse
import com.example.holoverse.courses.domain.CourseSession
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.utils.Response

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

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<AppCategory?>(null) }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isLevelExpanded by remember { mutableStateOf(false) }
    var showSessionDialog by remember { mutableStateOf(false) }
    var editingSessionIndex by remember { mutableStateOf<Int?>(null) }

    var showBoostConfirmation by remember { mutableStateOf(false) }
    var showAdSelection by remember { mutableStateOf(false) }
    var showPlanSelection by remember { mutableStateOf(false) }
    var showPaymentSimulation by remember { mutableStateOf(false) }

    var selectedAdStyle by remember { mutableStateOf<AdCardStyle?>(null) }
    var selectedPlanDuration by remember { mutableStateOf("") }

    val categories = AppCategory.entries.filter { it != AppCategory.OTHER }

    val levels = listOf("Beginner", "Intermediate", "Advanced")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadImage(it) }
        }
    )

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
        modifier = Modifier.background(Brush(darkTheme)),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))
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
                        IconButton(onClick = { onCourseCreated() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            }
        }
    ) { paddingValues ->
        if (showBoostConfirmation) {
            BoostConfirmationDialog(
                onDismiss = {
                    showBoostConfirmation = false
                    onCourseCreated()
                },
                onConfirm = {
                    showBoostConfirmation = false
                    showAdSelection = true
                }
            )
        }

        if (showAdSelection && lastCreatedCourse != null) {
            AdCardSelectionDialog(
                course = lastCreatedCourse!!,
                onDismiss = {
                    showAdSelection = false
                    onCourseCreated()
                },
                onStyleSelected = { style ->
                    selectedAdStyle = style
                    showAdSelection = false
                    showPlanSelection = true
                }
            )
        }

        if (showPlanSelection) {
            SubscriptionPlanDialog(
                onDismiss = {
                    showPlanSelection = false
                    onCourseCreated()
                },
                onPlanSelected = { duration, _ ->
                    selectedPlanDuration = duration
                    showPlanSelection = false
                    showPaymentSimulation = true
                }
            )
        }

        if (showPaymentSimulation) {
            PaymentSimulationDialog(
                onSuccess = {
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
                }
            )
        }

        if (showSessionDialog) {
            val sessionToEdit = editingSessionIndex?.let { viewModel.sessions.getOrNull(it) }
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
                // Course Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
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
                            Text("Tap to upload course image", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (uploadImageState is Response.Loading) {
                        CircularProgressIndicator(color = HoloCyan)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoloCyan)
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Course Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        RadioButtonMenu(
                            isExpanded = isCategoryExpanded,
                            onToggle = { isCategoryExpanded = !isCategoryExpanded },
                            selectedItem = category?.let { stringResource(it.titleRes) } ?: "Select Category",
                            onItemSelected = { item ->
                                category = categories.find { context.getString(it.titleRes) == item }
                                isCategoryExpanded = false
                            },
                            menuItems = categories.map { stringResource(it.titleRes) },
                            showIcon = false
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Price ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
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
                                level = item
                                isLevelExpanded = false
                            },
                            menuItems = levels,
                            showIcon = false
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoloCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Write about the course...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Syllabus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = {
                            editingSessionIndex = null
                            showSessionDialog = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HoloPurple)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Session", tint = Color.White)
                    }
                }
            }

            if (viewModel.sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sessions added yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            itemsIndexed(viewModel.sessions) { index, session ->
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
                Spacer(modifier = Modifier.height(16.dp))
                if (createCourseState is Response.Loading) {
                    CircularProgressIndicator(color = HoloPurple)
                } else {
                    Button(
                        onClick = {
                            if (name.isBlank() || category == null || price.isBlank() || imageUrl.isBlank() || level.isBlank()) {
                                val message = when {
                                    imageUrl.isBlank() -> "Please upload a course image"
                                    category == null -> "Please select a category"
                                    level.isBlank() -> "Please select a course level"
                                    else -> "Please fill required fields"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createCourse(
                                    name = name,
                                    category = category!!,
                                    price = price,
                                    duration = duration,
                                    level = level,
                                    description = description,
                                    imageUrl = imageUrl
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
                    ) {
                        Text("Create Course", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = HoloCyan, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = HoloCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.date, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = HoloCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.time, style = MaterialTheme.typography.bodySmall)
            }
        }
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
