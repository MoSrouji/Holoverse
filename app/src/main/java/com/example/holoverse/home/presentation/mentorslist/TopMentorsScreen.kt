package com.example.holoverse.home.presentation.mentorslist

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.composeautoshimmer.components.ShimmerBox
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.home.presentation.HomeViewModel
import com.example.holoverse.reviews.presentation.ui.components.LiveMentorRating
import com.example.holoverse.core.ui.theme.HoloverseTheme
import com.example.holoverse.core.ui.theme.IbarraNovaFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMentorsScreen(
    onBackClick: () -> Unit,
    onMentorClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    darkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val mentors = uiState.allMentors

    val categories = remember(mentors) {
        listOf(AppCategory.OTHER) + mentors.map { it.specialization }
            .distinct()
            .filter { it != AppCategory.OTHER }
            .sortedBy { it.name }
    }
    var selectedCategory by remember { mutableStateOf(AppCategory.OTHER) }

    val filteredMentors = if (uiState.searchQuery.isBlank()) {
        mentors.filter {
            (selectedCategory == AppCategory.OTHER || it.specialization == selectedCategory)
        }
    } else {
        uiState.mentorSearchResults
    }

    val listState = rememberLazyListState()

    // Infinity Scroll Detection
    androidx.compose.runtime.LaunchedEffect(listState, filteredMentors) {
        if (filteredMentors.size < 10 && !uiState.isLoading && uiState.lastMentorDocument != null) {
            viewModel.loadMoreMentors()
        }

        androidx.compose.runtime.snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= filteredMentors.size - 5 && !uiState.isLoading && uiState.lastMentorDocument != null) {
                    viewModel.loadMoreMentors()
                }
            }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Top Mentors",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_mentors),
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = categories,
                    contentType = { "category_chip" }
                ) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                stringResource(category.titleRes),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.searchQuery.isEmpty()) "Showing ${filteredMentors.size} mentors" else "Search results (${filteredMentors.size})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            ShimmerBox(
                isLoading = (uiState.isLoading || uiState.isSearching) && filteredMentors.isEmpty(),
                baseColor = Color.DarkGray,
                durationMillis = 800
            ) {
                if (filteredMentors.isEmpty() && !(uiState.isLoading || uiState.isSearching)) {
                    EmptyMentorState()
                } else {
                    val displayMentors = if ((uiState.isLoading || uiState.isSearching) && filteredMentors.isEmpty()) {
                        List(6) {
                            User.Mentor(
                                userId = "shimmer_$it",
                                fullName = "Loading Mentor...",
                                profileImageUrl = null,
                                bio = "Loading mentor bio information..."
                            )
                        }
                    } else filteredMentors

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = displayMentors,
                            key = { it.userId ?: "shimmer_${it.hashCode()}" },
                            contentType = { "mentor_item" }
                        ) { mentor ->
                            MentorListItem(
                                mentor = mentor,
                                onClick = {
                                    if (!uiState.isLoading) mentor.userId?.let {
                                        onMentorClick(
                                            it
                                        )
                                    }
                                }
                            )
                        }

                        if (uiState.isPaginatingMentors) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMentorState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No mentors found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Try adjusting your filters or search query",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MentorListItem(
    mentor: User.Mentor,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(110.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = mentor.profileImageUrl,
                contentDescription = mentor.fullName,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                error = painterResource(R.drawable.istockphoto_1934800957_612x612)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = mentor.fullName ?: "Unknown Mentor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = mentor.specialization.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LiveMentorRating(
                        mentorId = mentor.userId ?: "",
                        initialRating = mentor.averageRating ?: 0.0,
                        initialReviewsCount = mentor.reviewsCount ?: 0,
                        textStyle = MaterialTheme.typography.bodySmall,
                        iconSize = 14.dp,
                        showReviewsCount = true,
                        reviewsCountStyle = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        iconTint = Color(0xFFFFC107)
                    )
                    Text("|", color = Color.LightGray)
                    Text(
                        text = "${formatValue(mentor.totalStudentsTaught ?: 0)} students",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Text(
                    text = mentor.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatValue(num: Int): String {
    return if (num >= 1000) {
        String.format(java.util.Locale.US, "%.1fk", num / 1000.0)
    } else {
        num.toString()
    }
}


@Preview(showBackground = true)
@Composable
fun TopMentorsScreenPreview() {
    HoloverseTheme {
        TopMentorsScreen(onBackClick = {}, onMentorClick = {})
    }
}


