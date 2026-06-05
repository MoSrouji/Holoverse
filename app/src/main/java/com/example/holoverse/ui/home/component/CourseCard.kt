package com.example.holoverse.ui.home.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import androidx.compose.ui.res.stringResource
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.Courses

@Composable
fun CourseCard(
    course: Courses,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(240.dp)
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.50f)
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                AsyncImage(
                    model = course.imageUrl,
                    contentDescription = course.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                    error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                CourseTypeWithButton(
                    category = stringResource(course.category.titleRes),
                    isSaved = isSaved,
                    onSaveClick = onSaveClick
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.padding(4.dp))
                CourseBottomDivider(
                    price = course.price,
                    rating = course.rating,
                    numEnrolled = course.numEnrolled
                )
            }
        }
    }
}

@Composable
fun CourseTypeWithButton(
    category: String,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
            contentDescription = "Save For Later",
            modifier = Modifier.clickable { onSaveClick() },
            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CourseBottomDivider(price: Double, rating: Double, numEnrolled: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$${"%.2f".format(price)}", fontWeight = FontWeight.Bold)
        VerticalDivider(modifier = Modifier.height(16.dp), thickness = 1.dp)
        Text(text = rating.toString())
        VerticalDivider(modifier = Modifier.height(16.dp), thickness = 1.dp)
        Text(text = "$numEnrolled Std")
    }
}

@Composable
@Preview(showBackground = true)
fun CourseCardPreview() {
    CourseCard(
        course = Courses(
            name = "Graphic Design Advanced",
            category = AppCategory.GRAPHIC_DESIGN,
            price = 28.0,
            rating = 4.2,
            numEnrolled = 7830
        )
    )
}
