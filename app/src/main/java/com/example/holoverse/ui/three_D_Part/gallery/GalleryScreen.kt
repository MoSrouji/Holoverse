package com.example.holoverse.ui.three_D_Part.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.three_D_Part.ModelViewModel

@Composable
fun GalleryScreen(
    appNavigator: AppNavigator,
    darkTheme: Boolean,
    viewModel: ModelViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredModels by viewModel.filteredModels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    FullGalleryScreen(
        models = filteredModels,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        categories = categories,
        selectedCategory = uiState.selectedCategory,
        onCategorySelected = { viewModel.updateSelectedCategory(it) },
        onModelSelected = { model ->
            viewModel.selectModel(model)
            appNavigator.navigateTo(AppDestination.ViewerScreen )
        },
        onBackClick = { appNavigator.popBackStack() },
        onRefresh = { viewModel.onRefresh() },
        isLoading = uiState.isLoading,
        darkTheme = darkTheme
    )
}
