package com.example.holoverse.ui.three_D_Part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.three_d_model.domain.model.Model
import com.example.holoverse.three_d_model.domain.usecase.GetModelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.category

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val getModelsUseCase: GetModelsUseCase
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
                val models = getModelsUseCase(forceRefresh)
                _uiState.update {
                    it.copy(
                        models = models,
                        selectedModel = models.firstOrNull(),
                        isLoading = false
                    )
                }
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
        _uiState.value = _uiState.value.copy(selectedModel = model)
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
}
