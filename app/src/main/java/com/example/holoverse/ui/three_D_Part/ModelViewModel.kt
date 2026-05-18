package com.example.holoverse.ui.three_D_Part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.three_d_model.data.local.ModelCacheManager
import com.example.holoverse.three_d_model.domain.model.Model
import com.example.holoverse.three_d_model.domain.usecase.GetModelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val getModelsUseCase: GetModelsUseCase,
    val cacheManager: ModelCacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState.asStateFlow()

    val filteredModels: StateFlow<List<Model>> = _uiState
        .map { state ->
            state.models.filter { model ->
                val matchesSearch = model.name.contains(state.searchQuery, ignoreCase = true) ||
                        model.description.contains(state.searchQuery, ignoreCase = true)
                val matchesCategory = state.selectedCategory == "All" || model.category == state.selectedCategory
                matchesSearch && matchesCategory
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = _uiState
        .map { state ->
            val modelCategories = state.models.map { it.category }.distinct().filter { it != "All" }.sorted()
            listOf("All") + modelCategories
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    init {
        fetchModels()
    }

    private fun fetchModels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val modelsList = getModelsUseCase(forceRefresh)
                val firstModel = modelsList.firstOrNull()
                _uiState.update {
                    it.copy(
                        models = modelsList,
                        isLoading = false
                    )
                }
                // Select the first model if available
                firstModel?.let { selectModel(it) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        fetchModels(forceRefresh = true)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun selectModel(model: Model) {
        _uiState.update { state ->
            // Move the selected model to the first position in the list
            val updatedModels = state.models.filter { it.id != model.id }.toMutableList()
            updatedModels.add(0, model)
            
            state.copy(
                selectedModel = model,
                models = updatedModels,
                error = null
            )
        }
        // Pre-cache the model in the background as soon as it's selected
        viewModelScope.launch {
            try {
                cacheManager.getModelPath(model.id, model.path) { progress, downloaded, total ->
                    _uiState.update { state ->
                        val updatedProgress = state.downloadProgress.toMutableMap()
                        updatedProgress[model.id] = DownloadProgress(progress, downloaded, total)
                        state.copy(downloadProgress = updatedProgress)
                    }
                }
            } catch (e: Exception) {
                // Background caching failure is silent here; ArScreen will handle it if it fails again
            }
        }
    }

    fun setShowModelGallery(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModelGallery = show)
    }

    fun updateRotation(rotation: Float) {
        _uiState.value = _uiState.value.copy(modelRotation = rotation)
    }

    fun updateScale(scale: Float) {
        _uiState.value = _uiState.value.copy(modelScale = scale)
    }

    fun resetTransformations() {
        _uiState.value = _uiState.value.copy(modelRotation = 0f, modelScale = 1f)
    }

    fun addLocalModel(name: String, path: String) {
        val newModel = Model(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            path = path,
            description = "Local model from storage",
            category = "Local"
        )
        _uiState.update { state ->
            val updatedModels = listOf(newModel) + state.models
            state.copy(
                models = updatedModels,
                selectedModel = newModel
            )
        }
    }
}
