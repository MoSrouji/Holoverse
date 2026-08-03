package com.example.holoverse.threedmodel.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.domain.use_cases.GetCurrentUser
import com.example.holoverse.cloudinaryservices.domain.repository.CloudinaryRepository
import com.example.holoverse.threedmodel.data.local.ModelCacheManager
import com.example.holoverse.threedmodel.domain.model.Model
import com.example.holoverse.threedmodel.domain.usecase.GetModelsUseCase
import com.example.holoverse.threedmodel.domain.usecase.UploadModelUseCase
import com.example.holoverse.material.domain.model.Material
import com.example.holoverse.material.domain.model.MaterialType
import com.example.holoverse.material.domain.usecase.CheckMaterialSavedUseCase
import com.example.holoverse.material.domain.usecase.SaveMaterialUseCase
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.core.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val getModelsUseCase: GetModelsUseCase,
    private val uploadModelUseCase: UploadModelUseCase,
    private val saveMaterialUseCase: SaveMaterialUseCase,
    private val checkMaterialSavedUseCase: CheckMaterialSavedUseCase,
    private val getCurrentUser: GetCurrentUser,
    private val cloudinaryRepository: CloudinaryRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager,
    val cacheManager: ModelCacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState.asStateFlow()

    private var selectionJob: Job? = null

    private val _currentUser = MutableStateFlow<User?>(null)
    val isMentor: StateFlow<Boolean> = _currentUser.map { it?.accountType == UserType.Mentor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val filteredModels: StateFlow<List<Model>> = _uiState
        .map { state ->
            state.models.filter { model ->
                val matchesSearch = model.name.contains(state.searchQuery, ignoreCase = true) ||
                        model.description.contains(state.searchQuery, ignoreCase = true)
                val matchesCategory =
                    state.selectedCategory == "All" || model.category == state.selectedCategory
                matchesSearch && matchesCategory
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = _uiState
        .map { state ->
            val modelCategories = state.models.map { it.category }.distinct().sorted()
            listOf("All") + modelCategories
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    init {
        fetchModels()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = getCurrentUser()
        }
    }

    fun uploadModel(
        name: String,
        description: String,
        category: String,
        price: Double,
        modelUri: Uri,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0.1f, error = null) }
            
            try {
                // 1. Upload model file
                val modelUploadResult = cloudinaryRepository.uploadFile(modelUri)
                if (modelUploadResult.isFailure) {
                    throw Exception("Failed to upload model file: ${modelUploadResult.exceptionOrNull()?.message}")
                }
                val modelData = modelUploadResult.getOrThrow()
                val modelUrl = modelData.url
                _uiState.update { it.copy(uploadProgress = 0.5f) }

                // 2. Upload image file if exists OR generate thumbnail
                var finalImageUrl: String? = null
                if (imageUri != null) {
                    val imageUploadResult = cloudinaryRepository.uploadFile(imageUri)
                    if (imageUploadResult.isSuccess) {
                        finalImageUrl = imageUploadResult.getOrThrow().url
                    }
                } else {
                    // Generate automatic thumbnail from the uploaded model if it's a GLB
                    val isGlb = try {
                        val path = modelUri.path ?: ""
                        path.lowercase().endsWith(".glb") || modelUrl.lowercase().contains(".glb")
                    } catch (e: Exception) {
                        modelUrl.lowercase().contains(".glb")
                    }
                    
                    if (isGlb) {
                        finalImageUrl = cloudinaryRepository.get3DThumbnailUrl(modelData.publicId)
                    }
                }
                _uiState.update { it.copy(uploadProgress = 0.8f) }

                // 3. Save metadata to Firestore
                val user = _currentUser.value
                val newModel = Model(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    path = modelUrl,
                    description = description,
                    category = category,
                    imageUrl = finalImageUrl,
                    price = price,
                    uploadedBy = user?.fullName ?: "Mentor",
                    uploadedById = user?.userId
                )

                val result = uploadModelUseCase(newModel)
                if (result.isSuccess) {
                    android.util.Log.d("ModelViewModel", "Model uploaded successfully: ${newModel.name}, Image: ${newModel.imageUrl}")
                    _uiState.update { it.copy(isUploading = false, uploadProgress = 1.0f) }
                    fetchModels(forceRefresh = true)
                } else {
                    throw Exception("Failed to save model metadata: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    private fun fetchModels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val rawModels = getModelsUseCase(forceRefresh)
                val targetLang = preferenceManager.getLanguage() ?: "en"
                val modelsList = if (targetLang != "en") {
                    rawModels.map { model ->
                        model.copy(
                            name = translationManager.translate(model.name, targetLang = targetLang),
                            description = translationManager.translate(model.description, targetLang = targetLang)
                        )
                    }
                } else {
                    rawModels
                }

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
        selectionJob?.cancel()
        _uiState.update { state ->
            // Move the selected model to the first position in the list
            val updatedModels = state.models.filter { it.id != model.id }.toMutableList()
            updatedModels.add(0, model)

            state.copy(
                selectedModel = model,
                selectedModelPath = null, // Reset path while loading new selection
                isModelSaved = false, // Reset saved status
                models = updatedModels,
                error = null
            )
        }

        selectionJob = viewModelScope.launch {
            try {
                // Check if model is already saved
                _currentUser.value?.userId?.let { uid ->
                    val isSaved = checkMaterialSavedUseCase(uid, model.id)
                    _uiState.update { it.copy(isModelSaved = isSaved) }
                }

                val path = cacheManager.getModelPath(model.id, model.path) { progress, downloaded, total ->
                    _uiState.update { state ->
                        val updatedProgress = state.downloadProgress.toMutableMap()
                        updatedProgress[model.id] = DownloadProgress(progress, downloaded, total)
                        state.copy(downloadProgress = updatedProgress)
                    }
                }
                _uiState.update { it.copy(selectedModelPath = path) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load model: ${e.message}") }
            }
        }
    }

    fun saveModelToMaterial(model: Model) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val uid = user.userId ?: return@launch
            
            val material = Material(
                id = model.id,
                name = model.name,
                type = MaterialType.MODEL,
                url = model.path,
                thumbnailUrl = model.imageUrl,
                description = model.description
            )
            
            val result = saveMaterialUseCase(uid, material)
            if (result.isSuccess) {
                _uiState.update { it.copy(isModelSaved = true) }
            } else {
                _uiState.update { it.copy(error = "Failed to save model: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun setShowModelGallery(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModelGallery = show)
    }

    fun updateRotation(rotation: Float) {
        _uiState.value = _uiState.value.copy(modelRotation = rotation)
    }

    fun updateVerticalRotation(rotation: Float) {
        _uiState.value = _uiState.value.copy(modelVerticalRotation = rotation)
    }

    fun updateScale(scale: Float) {
        _uiState.value = _uiState.value.copy(modelScale = scale)
    }

    fun resetTransformations() {
        _uiState.value = _uiState.value.copy(
            modelRotation = 0f,
            modelVerticalRotation = 0f,
            modelScale = 1f
        )
    }

    fun addLocalModel(name: String, path: String) {
        val newModel = Model(
            id = UUID.randomUUID().toString(),
            name = name,
            path = path,
            description = "Local model from storage",
            category = "Other",
            uploadedBy = "Local User"
        )
        selectionJob?.cancel()
        _uiState.update { state ->
            val updatedModels = listOf(newModel) + state.models
            state.copy(
                models = updatedModels,
                selectedModel = newModel,
                selectedModelPath = path
            )
        }
    }
}
