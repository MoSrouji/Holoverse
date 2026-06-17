package com.example.holoverse.three_d_model.domain.model

data class Model(
    val id: String,
    val name: String,
    val path: String,
    val description: String,
    val category: String = "Other",
    val imageUrl: String? = null
)
