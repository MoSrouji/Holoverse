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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.core.domain.model.AppCategory
import androidx.compose.ui.res.stringResource
import com.example.composeautoshimmer.components.ShimmerBox

@Composable
fun TextListButton(
    categories: List<AppCategory>,
    isLoading: Boolean = false,
    selectedCategory: AppCategory = AppCategory.OTHER,
    onCategoryClick: (AppCategory) -> Unit = {}
) {
    ShimmerBox(
        isLoading = isLoading && categories.size <= 1,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        val displayCategories = if (isLoading && categories.size <= 1) {
            listOf(AppCategory.OTHER)
        } else categories

        LazyRow(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayCategories) { item ->
                val isSelected = selectedCategory == item
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W600,
                    modifier = Modifier.clickable(enabled = !isLoading) { onCategoryClick(item) }
                )
            }
        }
    }
}

@Composable
fun TextListTextButton(
    categories: List<AppCategory>,
    isLoading: Boolean = false,
    selectedCategory: AppCategory = AppCategory.OTHER,
    onCategoryClick: (AppCategory) -> Unit = {}
) {
    ShimmerBox(
        isLoading = isLoading && categories.size <= 1,
        baseColor = Color.DarkGray,
        durationMillis = 800
    ) {
        val displayCategories = if (isLoading && categories.size <= 1) {
            listOf(AppCategory.OTHER)
        } else categories

        LazyRow(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayCategories) { item ->
                val isSelected = selectedCategory == item
                TextButton(
                    onClick = { onCategoryClick(item) },
                    enabled = !isLoading,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surfaceDim,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextListButtonPreview() {
}

@Preview(showBackground = true)
@Composable
fun TextListTextButtonPreview() {
}
