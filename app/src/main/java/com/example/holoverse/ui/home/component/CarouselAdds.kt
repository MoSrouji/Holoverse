package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.theme.ColorVerdigris
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import kotlinx.coroutines.delay

@Composable
fun CarouselCourses(courses: List<Courses>) {
    if (courses.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { courses.size }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged) {
        if (!isDragged) {
            while (true) {
                delay(5000)
                if (pagerState.pageCount > 0) {
                    val target = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(target)
                }
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val course = courses[page]
        val style = page % 3
        CoursePromotionalCard(
            course = course,
            style = style,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CoursePromotionalCard(course: Courses, style: Int, modifier: Modifier = Modifier) {
    when (style) {
        0 -> PromotionalStyleHolographic(course, modifier)
        1 -> PromotionalStyleFeatured(course, modifier)
        else -> PromotionalStyleDynamicSplit(course, modifier)
    }
}

@Composable
fun PromotionalStyleHolographic(course: Courses, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .height(160.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        HoloPurple.copy(alpha = 0.8f),
                        HoloCyan.copy(alpha = 0.6f),
                        ColorVerdigris.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "FEATURED COURSE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.height(16.dp)
                )
                Text(
                    text = " ${"%.2f".format(course.rating)}  •  ${stringResource(course.category.titleRes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PromotionalStyleFeatured(course: Courses, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .height(160.dp)
    ) {
        AsyncImage(
            model = course.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
            error = painterResource(R.drawable.istockphoto_1934800957_612x612)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(HoloCyan)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HOT NOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "Enroll for $${course.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = HoloCyan,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PromotionalStyleDynamicSplit(course: Courses, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .height(160.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            AsyncImage(
                model = course.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                error = painterResource(R.drawable.istockphoto_1934800957_612x612)
            )
        }
        Column(
            modifier = Modifier
                .weight(1.2f)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(course.category.titleRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "${course.numEnrolled}+",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Students",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = HoloPurple,
                    modifier = Modifier
                        .background(
                            HoloPurple.copy(alpha = 0.1f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun CarouselAdds(isLoading: Boolean = false) {
    val dummyCourses = remember {
        listOf(
            Courses(
                name = "Mastering Augmented Reality",
                category = AppCategory.ARTS,
                price = 49.99,
                rating = 4.9,
                numEnrolled = 1250,
                imageUrl = "https://images.unsplash.com/photo-1633177317976-3f9bc45e1d1d?q=80&w=320&h=160&auto=format&fit=crop"
            ),
            Courses(
                name = "VR World Building",
                category = AppCategory.COMPUTER_SCIENCE,
                price = 59.99,
                rating = 4.8,
                numEnrolled = 850,
                imageUrl = "https://images.unsplash.com/photo-1622979135225-d2ba269cf1ac?q=80&w=320&h=160&auto=format&fit=crop"
            ),
            Courses(
                name = "Spatial UI Design",
                category = AppCategory.ARTS,
                price = 39.99,
                rating = 4.7,
                numEnrolled = 2100,
                imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=320&h=160&auto=format&fit=crop"
            ),
            Courses(
                name = "Advanced Holographics",
                category = AppCategory.SCIENCE,
                price = 79.99,
                rating = 5.0,
                numEnrolled = 450,
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=320&h=160&auto=format&fit=crop"
            ),
            Courses(
                name = "Mixed Reality for Beginners",
                category = AppCategory.COMPUTER_SCIENCE,
                price = 29.99,
                rating = 4.5,
                numEnrolled = 3000,
                imageUrl = "https://images.unsplash.com/photo-1592477383748-47209930f46c?q=80&w=320&h=160&auto=format&fit=crop"
            ),
            Courses(
                name = "Unity XR Foundations",
                category = AppCategory.COMPUTER_SCIENCE,
                price = 69.99,
                rating = 4.6,
                numEnrolled = 1100,
                imageUrl = "https://images.unsplash.com/photo-1478416215748-28c169d5180f?q=80&w=320&h=160&auto=format&fit=crop"
            )
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dummyCourses.size }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged) {
        if (!isDragged) {
            while (true) {
                delay(5000)
                if (pagerState.pageCount > 0) {
                    val target = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(target)
                }
            }
        }
    }

    com.example.composeautoshimmer.components.ShimmerBox(
        isLoading = isLoading,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            CoursePromotionalCard(
                course = dummyCourses[page],
                style = page % 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
@Preview
fun CarouselAddsCards(text: String = " Enter\nAR Lab") {
    Box(
        modifier = Modifier
            .clip(
                shape = RoundedCornerShape(12.dp)
            )
            .height(160.dp)
            .width(320.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        HoloPurple.copy(alpha = 0.7f),
                        HoloCyan.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCoursePromotionalCards() {
    val sampleCourse = Courses(
        name = "Mastering Augmented Reality with ARCore",
        category = AppCategory.ARTS,
        price = 49.99,
        rating = 4.9,
        numEnrolled = 1250,
        imageUrl = "https://example.com/image.jpg"
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        CoursePromotionalCard(course = sampleCourse, style = 0)
        CoursePromotionalCard(course = sampleCourse, style = 1)
        CoursePromotionalCard(course = sampleCourse, style = 2)
    }
}
