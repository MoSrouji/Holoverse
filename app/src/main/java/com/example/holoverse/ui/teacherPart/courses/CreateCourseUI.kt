package com.example.holoverse.ui.teacherPart.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.auth.domain.entities.TeacherCategory
import com.example.holoverse.ui.commonPart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.spatialTheme.ProfileTextField
import com.example.holoverse.ui.theme.HoloverseTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateCourseViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var isLevelMenuExpanded by remember { mutableStateOf(false) }

    val categories = TeacherCategory.getAllCategoryNames()
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Expert")

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CreateCourseViewModel.UiEvent.SaveCourse -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Course") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Course Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            ProfileTextField(
                label = "Course Name",
                value = viewModel.name,
                error = null,
                onValueChange = { viewModel.onEvent(CreateCourseEvent.EnteredName(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            RadioButtonMenu(
                isExpanded = isCategoryMenuExpanded,
                onToggle = { isCategoryMenuExpanded = !isCategoryMenuExpanded },
                selectedItem = viewModel.category.ifEmpty { "Select Category" },
                onItemSelected = {
                    viewModel.onEvent(CreateCourseEvent.EnteredCategory(it))
                    isCategoryMenuExpanded = false
                },
                menuItems = categories,
                showIcon = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(0.85f)) {
                OutlinedTextField(
                    value = viewModel.price,
                    onValueChange = { viewModel.onEvent(CreateCourseEvent.EnteredPrice(it)) },
                    label = { Text("Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = viewModel.duration,
                    onValueChange = { viewModel.onEvent(CreateCourseEvent.EnteredDuration(it)) },
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonMenu(
                isExpanded = isLevelMenuExpanded,
                onToggle = { isLevelMenuExpanded = !isLevelMenuExpanded },
                selectedItem = viewModel.level.ifEmpty { "Select Level" },
                onItemSelected = {
                    viewModel.onEvent(CreateCourseEvent.EnteredLevel(it))
                    isLevelMenuExpanded = false
                },
                menuItems = levels,
                showIcon = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileTextField(
                label = "Description",
                value = viewModel.description,
                error = null,
                onValueChange = { viewModel.onEvent(CreateCourseEvent.EnteredDescription(it)) },
                singleLine = false
            )

            ProfileTextField(
                label = "Image URL",
                value = viewModel.imageUrl,
                error = null,
                onValueChange = { viewModel.onEvent(CreateCourseEvent.EnteredImageUrl(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onEvent(CreateCourseEvent.SaveCourse) },
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Create Course", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Preview
@Composable
private fun CreateCourseUiPreview() {
    HoloverseTheme{
        CreateCourseScreen(
            onNavigateBack = {}
        )
    }


}
