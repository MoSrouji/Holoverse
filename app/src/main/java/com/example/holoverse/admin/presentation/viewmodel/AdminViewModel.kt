package com.example.holoverse.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.admin.domain.repository.AdminRepository
import com.example.holoverse.admin.domain.repository.Timeframe
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val studentCount: Int = 0,
    val mentorCount: Int = 0,
    val courseCount: Int = 0,
    val categoryDistribution: Map<String, Int> = emptyMap(),
    val userGrowthData: List<Pair<String, Int>> = emptyList(),
    val totalRevenue: Double = 0.0,
    val selectedTimeframe: Timeframe = Timeframe.WEEK,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            launch {
                adminRepository.getStudentCount().collect { response ->
                    handleResponse(response) { count -> _uiState.update { it.copy(studentCount = count) } }
                }
            }
            launch {
                adminRepository.getMentorCount().collect { response ->
                    handleResponse(response) { count -> _uiState.update { it.copy(mentorCount = count) } }
                }
            }
            launch {
                adminRepository.getCourseCount().collect { response ->
                    handleResponse(response) { count -> _uiState.update { it.copy(courseCount = count) } }
                }
            }
            launch {
                adminRepository.getCoursesByCategory().collect { response ->
                    handleResponse(response) { dist -> _uiState.update { it.copy(categoryDistribution = dist) } }
                }
            }
            launch {
                loadGrowthData(Timeframe.WEEK)
            }
            launch {
                adminRepository.getTotalRevenue().collect { response ->
                    handleResponse(response) { rev -> _uiState.update { it.copy(totalRevenue = rev) } }
                }
            }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onTimeframeSelected(timeframe: Timeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
        viewModelScope.launch {
            loadGrowthData(timeframe)
        }
    }

    private suspend fun loadGrowthData(timeframe: Timeframe) {
        adminRepository.getUserGrowthData(timeframe).collect { response ->
            handleResponse(response) { data -> 
                _uiState.update { it.copy(userGrowthData = data) } 
            }
        }
    }

    private fun <T> handleResponse(response: Response<T>, onSuccess: (T) -> Unit) {
        when (response) {
            is Response.Success -> onSuccess(response.data)
            is Response.Error -> _uiState.update { it.copy(error = response.message) }
            is Response.Loading -> { /* Handled at start of loadDashboardData */ }
        }
    }
}

