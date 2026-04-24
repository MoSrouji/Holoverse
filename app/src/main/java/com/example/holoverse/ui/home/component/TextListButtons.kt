package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextListButton(
    categories: List<String>,
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier.padding(5.dp) ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { item ->
            val isSelected = (selectedCategory ?: "All") == item
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryFixedVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W600,
                modifier = Modifier.clickable { onCategoryClick(item) }
            )
        }
    }
}

@Composable
fun TextListTextButton(
    categories: List<String>,
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier.padding(5.dp) ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { item ->
            val isSelected = (selectedCategory ?: "All") == item
            TextButton(
                onClick = { onCategoryClick(item) },
                modifier = Modifier.background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surfaceDim,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextListButtonPreview() {
    TextListButton(categories = listOf("All", "Programming", "Design"))
}

@Preview(showBackground = true)
@Composable
fun TextListTextButtonPreview() {
    TextListTextButton(categories = listOf("All", "Programming", "Design"))
}
