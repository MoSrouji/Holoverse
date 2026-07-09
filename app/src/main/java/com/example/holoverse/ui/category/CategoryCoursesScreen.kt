package com.example.holoverse.ui.category

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.home.courseslist.CourseItem
import com.example.holoverse.ui.spatialtheme.Brush
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.utils.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCoursesScreen(
    category: AppCategory,
    onBackClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    darkTheme: Boolean,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(category) {
        viewModel.initialize(category)
    }

    val coursesState = viewModel.coursesState.value
    val savedCourseIds by viewModel.savedCourseIds
    val savingCourseIds by viewModel.savingCourseIds
    val categoryName = stringResource(category.titleRes)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onBackClick() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }

                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = IbarraNovaFont
                            ),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explore top rated courses in $categoryName",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ShimmerBox(
                    isLoading = coursesState is Response.Loading,
                    baseColor = Color.DarkGray,
                    durationMillis = 800
                ) {
                    when (coursesState) {
                        is Response.Loading -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(300.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(6) {
                                    CourseItem(
                                        course = Courses(
                                            id = "shimmer_$it",
                                            name = "Loading Course Name...",
                                            category = AppCategory.ARTS,
                                            price = 0.0,
                                            rating = 0.0,
                                            numReviews = 0,
                                            numEnrolled = 0
                                        ),
                                        onClick = {}
                                    )
                                }
                            }
                        }

                        is Response.Success -> {
                            val courses = coursesState.data
                            if (courses.isEmpty()) {
                                Text(
                                    text = "No courses found in this category",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(300.dp),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = courses,
                                        key = { it.id },
                                        contentType = { "category_course_item" }
                                    ) { course ->
                                        CourseItem(
                                            course = course,
                                            onClick = { onCourseClick(course.id) },
                                            isSaved = savedCourseIds.contains(course.id),
                                            isSaving = savingCourseIds.contains(course.id),
                                            onSaveClick = { viewModel.toggleSaveCourse(course.id) }
                                        )
                                    }
                                }
                            }
                        }

                        is Response.Error -> {
                            Text(
                                text = coursesState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}
