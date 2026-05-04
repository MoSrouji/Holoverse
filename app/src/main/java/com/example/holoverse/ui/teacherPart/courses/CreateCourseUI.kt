package com.example.holoverse.ui.teacherPart.courses

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.utils.Response
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onCourseCreated: () -> Unit,
    viewModel: CreateCourseViewModel = hiltViewModel(),
    darkTheme: Boolean = true
) {
    val context = LocalContext.current
    val createCourseState by viewModel.createCourseState
    val uploadImageState by viewModel.uploadImageState

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isLevelExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        stringResource(R.string.category_3d_design),
        stringResource(R.string.category_graphic_design),
        stringResource(R.string.category_web_development),
        stringResource(R.string.category_seo_marketing),
        stringResource(R.string.category_finance_accounting),
        stringResource(R.string.category_personal_development),
        stringResource(R.string.category_office_productivity),
        stringResource(R.string.category_hr_management)
    )

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
                Toast.makeText(context, (uploadImageState as Response.Error).massage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(createCourseState) {
        when (createCourseState) {
            is Response.Success -> {
                Toast.makeText(context, "Course created successfully!", Toast.LENGTH_SHORT).show()
                onCourseCreated()
            }
            is Response.Error -> {
                Toast.makeText(context, (createCourseState as Response.Error).massage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                TopAppBar(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Tap to upload course image")
                    }
                }
                
                if (uploadImageState is Response.Loading) {
                    CircularProgressIndicator()
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection (Expandable Radio Button Menu)
            RadioButtonMenu(
                isExpanded = isCategoryExpanded,
                onToggle = { isCategoryExpanded = !isCategoryExpanded },
                selectedItem = category.ifEmpty { "Select Category" },
                onItemSelected = { item ->
                    category = item
                    isCategoryExpanded = false
                },
                menuItems = categories,
                showIcon = false
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (e.g., 10 hours)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Level Selection (Expandable Radio Button Menu)
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

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (createCourseState is Response.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (name.isBlank() || category.isBlank() || price.isBlank() || imageUrl.isBlank() || level.isBlank()) {
                            val message = when {
                                imageUrl.isBlank() -> "Please upload a course image"
                                category.isBlank() -> "Please select a category"
                                level.isBlank() -> "Please select a course level"
                                else -> "Please fill required fields"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createCourse(
                                name = name,
                                category = category,
                                price = price,
                                duration = duration,
                                level = level,
                                description = description,
                                imageUrl = imageUrl
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Course")
                }
            }
        }
    }
}
