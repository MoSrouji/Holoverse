package com.example.holoverse.material.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.use_cases.GetCurrentUser
import com.example.holoverse.core.utils.Response
import com.example.holoverse.material.domain.model.Material
import com.example.holoverse.material.domain.model.MaterialType
import com.example.holoverse.material.domain.usecase.GetMaterialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YourMaterialViewModel @Inject constructor(
    private val getMaterialsUseCase: GetMaterialsUseCase,
    private val getCurrentUser: GetCurrentUser
) : ViewModel() {

    private val _materialsState = MutableStateFlow<Response<List<Material>>>(Response.Loading)
    val materialsState: StateFlow<Response<List<Material>>> = _materialsState.asStateFlow()

    private val _selectedType = MutableStateFlow<MaterialType?>(null)
    val selectedType: StateFlow<MaterialType?> = _selectedType.asStateFlow()

    init {
        loadMaterials()
    }

    fun loadMaterials(type: MaterialType? = _selectedType.value) {
        viewModelScope.launch {
            val user = getCurrentUser()
            val uid = user?.userId ?: return@launch
            
            _selectedType.value = type
            
            getMaterialsUseCase(uid, type).collectLatest { response ->
                _materialsState.value = response
            }
        }
    }
}
