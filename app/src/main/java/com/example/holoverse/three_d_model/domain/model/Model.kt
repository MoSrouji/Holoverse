package com.example.holoverse.three_d_model.domain.model

import com.example.holoverse.core.domain.model.AppCategory

data class Model(
    val id: String,
    val name: String,
    val path: String,
    val description: String,
    val category: AppCategory = AppCategory.OTHER,
    val imageUrl: String? = null
)
