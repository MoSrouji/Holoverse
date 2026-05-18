package com.example.holoverse.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.home.component.CourseCard
import com.example.holoverse.ui.theme.ColorBlue
import com.example.holoverse.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onMentorClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    darkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

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
                            "Search",
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
        ) {
            SearchInputSection(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearchClick = { /* Already handled by debounce */ }
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
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { /* Open Filters Dialog */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
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
                .clickable { onSearchClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
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
            label = { Text("Courses") }
        )
        FilterChip(
            selected = selectedType == SearchType.MENTORS,
            onClick = { onTypeChange(SearchType.MENTORS) },
            label = { Text("Mentors") }
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
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "SEE ALL >",
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
                        contentDescription = "Remove",
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
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error, color = Color.Red)
        }
    } else {
        val isEmpty = if (uiState.searchType == SearchType.COURSES) {
            uiState.searchResults.courses.isEmpty()
        } else {
            uiState.searchResults.mentors.isEmpty()
        }

        if (isEmpty) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No results found for \"${uiState.query}\"")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.searchType == SearchType.COURSES) {
                    items(uiState.searchResults.courses) { course ->
                        CourseCard(
                            course = course,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCourseClick(course.id) }
                        )
                    }
                } else {
                    items(uiState.searchResults.mentors) { mentor ->
                        MentorSearchResultItem(
                            mentor = mentor,
                            onClick = { onMentorClick(mentor.userId ?: "") }
                        )
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
            model = "https://via.placeholder.com/150", // Placeholder if no image in mentor object
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mentor.fullName ?: "Unknown Mentor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = mentor.specialization.name,
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
                    text = " ${mentor.averageRating ?: 0.0} (${mentor.reviewsCount ?: 0} reviews)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = "$${"%.2f".format(mentor.hourlyRate ?: 0.0)}/hr",
            fontWeight = FontWeight.Bold,
            color = ColorBlue
        )
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
