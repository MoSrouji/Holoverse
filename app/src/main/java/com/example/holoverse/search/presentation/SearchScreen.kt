package com.example.holoverse.search.presentation

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.home.presentation.component.CourseCard
import com.example.holoverse.core.ui.theme.ColorBlue
import com.example.holoverse.core.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onMentorClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    darkTheme: Boolean = true,
    triggerVoice: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showFilters by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                viewModel.onQueryChange(results[0])
            }
        }
    }

    val startVoiceRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak ")
        }
        voiceLauncher.launch(intent)
    }

    LaunchedEffect(Unit) {
        if (triggerVoice) {
            startVoiceRecognition()
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FilterBottomSheetContent(
                uiState = uiState,
                onClose = { showFilters = false },
                onClear = viewModel::clearFilters,
                onUpdateCourseCategory = viewModel::updateCourseCategory,
                onUpdateCourseLevel = viewModel::updateCourseLevel,
                onUpdateCoursePrice = viewModel::updateCoursePriceRange,
                onUpdateMentorSpecialization = viewModel::updateMentorSpecialization,
                onUpdateMentorRate = viewModel::updateMentorHourlyRate,
                onUpdateMentorRating = viewModel::updateMentorRating
            )
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
                            stringResource(R.string.search),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = IbarraNovaFont,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
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
        ) {
            SearchInputSection(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onVoiceClick = startVoiceRecognition,
                onFilterClick = { showFilters = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchTypeTabs(
                selectedType = uiState.searchType,
                onTypeChange = viewModel::onSearchTypeChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.query.isEmpty()) {
                RecentSearchesSection(
                    recentSearches = uiState.recentSearches,
                    onItemClick = viewModel::onQueryChange,
                    onRemoveClick = viewModel::removeRecentSearch
                )
            } else {
                SearchResultsSection(
                    uiState = uiState,
                    onCourseClick = onCourseClick,
                    onMentorClick = onMentorClick
                )
            }
        }
    }
}

@Composable
fun SearchInputSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filter)
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorBlue)
                .clickable { onVoiceClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = stringResource(R.string.voice_search),
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTypeTabs(
    selectedType: SearchType,
    onTypeChange: (SearchType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedType == SearchType.COURSES,
            onClick = { onTypeChange(SearchType.COURSES) },
            label = { Text(stringResource(R.string.courses_tab)) }
        )
        FilterChip(
            selected = selectedType == SearchType.MENTORS,
            onClick = { onTypeChange(SearchType.MENTORS) },
            label = { Text(stringResource(R.string.mentors_tab)) }
        )
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<String>,
    onItemClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_searches),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(R.string.see_all),
                color = ColorBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* See all */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(recentSearches) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item, fontSize = 16.sp)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRemoveClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsSection(
    uiState: SearchUiState,
    onCourseClick: (String) -> Unit,
    onMentorClick: (String) -> Unit
) {
    ShimmerBox(
        isLoading = uiState.isLoading,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = Color.Red)
            }
        } else {
            val isEmpty = if (uiState.searchType == SearchType.COURSES) {
                uiState.searchResults.courses.isEmpty()
            } else {
                uiState.searchResults.mentors.isEmpty()
            }

            if (isEmpty && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_results_found, uiState.query))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.searchType == SearchType.COURSES) {
                        val displayCourses =
                            if (uiState.isLoading && uiState.searchResults.courses.isEmpty()) {
                                List(5) {
                                    com.example.holoverse.course.domain.Courses(
                                        id = "shimmer_$it",
                                        name = "loading mentor",
                                        specialization = "Loading..."
                                    )
                                }
                            } else uiState.searchResults.courses

                        items(
                            items = displayCourses,
                            key = { it.id },
                            contentType = { "course_search_result" }
                        ) { course ->
                            CourseCard(
                                course = course,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { if (!uiState.isLoading) onCourseClick(course.id) }
                            )
                        }
                    } else {
                        val displayMentors =
                            if (uiState.isLoading && uiState.searchResults.mentors.isEmpty()) {
                                List(5) {
                                    User.Mentor(
                                        userId = "shimmer_$it",
                                        fullName = "loading mentor"
                                    )
                                }
                            } else uiState.searchResults.mentors

                        items(
                            items = displayMentors,
                            key = { it.userId ?: it.hashCode() },
                            contentType = { "mentor_search_result" }
                        ) { mentor ->
                            MentorSearchResultItem(
                                mentor = mentor,
                                onClick = {
                                    if (!uiState.isLoading) onMentorClick(
                                        mentor.userId ?: ""
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MentorSearchResultItem(mentor: User.Mentor, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = mentor.profileImageUrl ?: "https://via.placeholder.com/150",
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mentor.fullName ?: stringResource(R.string.unknown_mentor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(mentor.specialization.titleRes),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB400),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " ${"%.1f".format(mentor.averageRating ?: 0.0)} ${
                        stringResource(
                            R.string.reviews_count,
                            mentor.reviewsCount ?: 0
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = stringResource(R.string.price_format, mentor.hourlyRate ?: 0.0) + stringResource(
                R.string.hourly_rate_suffix
            ),
            fontWeight = FontWeight.Bold,
            color = ColorBlue
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetContent(
    uiState: SearchUiState,
    onClose: () -> Unit,
    onClear: () -> Unit,
    onUpdateCourseCategory: (AppCategory?) -> Unit,
    onUpdateCourseLevel: (String?) -> Unit,
    onUpdateCoursePrice: (Double?, Double?) -> Unit,
    onUpdateMentorSpecialization: (AppCategory?) -> Unit,
    onUpdateMentorRate: (Double?, Double?) -> Unit,
    onUpdateMentorRating: (Double?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.filter),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                stringResource(R.string.clear_all),
                color = ColorBlue,
                modifier = Modifier.clickable { onClear() },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.searchType == SearchType.COURSES) {
            CourseFilterSection(
                filters = uiState.courseFilters,
                onUpdateCategory = onUpdateCourseCategory,
                onUpdateLevel = onUpdateCourseLevel,
                onUpdatePrice = onUpdateCoursePrice
            )
        } else {
            MentorFilterSection(
                filters = uiState.mentorFilters,
                onUpdateSpecialization = onUpdateMentorSpecialization,
                onUpdateRate = onUpdateMentorRate,
                onUpdateRating = onUpdateMentorRating
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorBlue)
        ) {
            Text(
                stringResource(R.string.apply_filters),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFilterSection(
    filters: CourseFilters,
    onUpdateCategory: (AppCategory?) -> Unit,
    onUpdateLevel: (String?) -> Unit,
    onUpdatePrice: (Double?, Double?) -> Unit
) {
    val categories = AppCategory.entries.filter { it != AppCategory.OTHER }
    val levels = listOf(
        stringResource(R.string.beginner),
        stringResource(R.string.intermediate),
        stringResource(R.string.advanced)
    )

    Text(
        stringResource(R.string.categories_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            FilterChip(
                selected = filters.category == category,
                onClick = { onUpdateCategory(if (filters.category == category) null else category) },
                label = { Text(stringResource(category.titleRes)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        stringResource(R.string.level),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        levels.forEach { level ->
            FilterChip(
                selected = filters.level == level,
                onClick = { onUpdateLevel(if (filters.level == level) null else level) },
                label = { Text(level) }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        stringResource(R.string.price_range),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    var sliderPosition by remember {
        mutableStateOf((filters.minPrice?.toFloat() ?: 0f)..(filters.maxPrice?.toFloat() ?: 500f))
    }
    RangeSlider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        valueRange = 0f..500f,
        onValueChangeFinished = {
            onUpdatePrice(sliderPosition.start.toDouble(), sliderPosition.endInclusive.toDouble())
        }
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.price_format, sliderPosition.start.toDouble()))
        Text(stringResource(R.string.price_format, sliderPosition.endInclusive.toDouble()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorFilterSection(
    filters: MentorFilters,
    onUpdateSpecialization: (AppCategory?) -> Unit,
    onUpdateRate: (Double?, Double?) -> Unit,
    onUpdateRating: (Double?) -> Unit
) {
    val categories = AppCategory.entries.filter { it != AppCategory.OTHER }

    Text(
        stringResource(R.string.specialization_label),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            FilterChip(
                selected = filters.specialization == category,
                onClick = { onUpdateSpecialization(if (filters.specialization == category) null else category) },
                label = { Text(stringResource(category.titleRes)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        stringResource(R.string.hourly_rate_label),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    var sliderPosition by remember {
        mutableStateOf(
            (filters.minHourlyRate?.toFloat() ?: 0f)..(filters.maxHourlyRate?.toFloat() ?: 200f)
        )
    }
    RangeSlider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        valueRange = 0f..200f,
        onValueChangeFinished = {
            onUpdateRate(sliderPosition.start.toDouble(), sliderPosition.endInclusive.toDouble())
        }
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            stringResource(
                R.string.price_format,
                sliderPosition.start.toDouble()
            ) + stringResource(R.string.hourly_rate_suffix)
        )
        Text(
            stringResource(
                R.string.price_format,
                sliderPosition.endInclusive.toDouble()
            ) + stringResource(R.string.hourly_rate_suffix)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        stringResource(R.string.min_rating),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(3.0, 4.0, 4.5).forEach { rating ->
            FilterChip(
                selected = filters.minRating == rating,
                onClick = { onUpdateRating(if (filters.minRating == rating) null else rating) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFB400),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(stringResource(R.string.rating_plus, rating))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(
        onBackClick = {},
        onCourseClick = {},
        onMentorClick = {}
    )
}



