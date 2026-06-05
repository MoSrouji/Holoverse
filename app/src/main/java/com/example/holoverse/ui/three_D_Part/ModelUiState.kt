package com.example.holoverse.ui.three_D_Part

import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.three_d_model.domain.model.Model


data class ModelUiState(
    val models: List<Model> = emptyList(),
    val selectedModel: Model? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showModelGallery: Boolean = true,
    val modelRotation: Float = 0f,
    val modelScale: Float = 1f,
    val searchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.OTHER,
    val downloadProgress: Map<String, DownloadProgress> = emptyMap()
)

data class DownloadProgress(
    val progress: Float, // 0.0 to 1.0
    val downloadedSize: Long,
    val totalSize: Long
)
