package com.example.holoverse.ui.popularCourses

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.R
import com.example.holoverse.ui.theme.HoloverseTheme

data class Course(
    val category: String,
    val title: String,
    val newPrice: Int,
    val oldPrice: Int,
    val rating: Float,
    val students: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularCoursesScreen() {
    val courses = remember {
        listOf(
            Course("Graphic Design", "Graphic Design Advanced", 28, 42, 4.2f, "7830 Std"),
            Course("Graphic Design", "Advertisement Design", 42, 61, 3.9f, "12680 Std"),
            Course("Programming", "Graphic Design Advanced", 37, 41, 4.2f, "990 Std"),
            Course("Web Development", "Web Developer conce..", 56, 71, 4.9f, "14580 Std"),
            Course("SEO & Marketing", "Digital Marketing...", 45, 60, 4.8f, "10230 Std")
        )
    }

    val categories = listOf("All", "Graphic Design", "3D Design", "Arts & Design", "Programming")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Popular Courses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    Chip(
                        label = category,
                        isSelected = category == selectedCategory,
                        onSelected = { selectedCategory = it }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseItem(course = course)
                }
            }
        }
    }
}

@Composable
fun Chip(label: String, isSelected: Boolean, onSelected: (String) -> Unit) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray.copy(alpha = 0.3f)
    val contentColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onSelected(label) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = contentColor, fontSize = 14.sp)
    }
}

@Composable
fun CourseItem(course: Course) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            //   modifier = Modifier.padding(12.dp)
        ) {
//            Box(
//                modifier = Modifier
//                    .size(110.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Color.Black)
//            )
            Image(
                painter = painterResource(id = R.drawable._c2a306853c02c630a95ee6d5922a5d0),
                contentDescription = "Course Image",
                modifier = Modifier
                    .width(110.dp)
                    .height(145.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        course.category,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "\$${course.newPrice}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "\$${course.oldPrice}",
                        textDecoration = TextDecoration.LineThrough,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${course.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("|", color = Color.LightGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(course.students, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PopularCoursesScreenPreview() {
    HoloverseTheme {
        PopularCoursesScreen()
    }
}
