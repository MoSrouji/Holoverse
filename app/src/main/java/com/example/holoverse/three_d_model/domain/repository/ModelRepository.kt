package com.example.holoverse.three_d_model.domain.repository

import com.example.holoverse.three_d_model.domain.model.Model

interface ModelRepository {
    suspend fun getModels(forceRefresh: Boolean = false): List<Model>
}
