package com.example.holoverse.three_d_model.domain.model

data class Model(
    val id: String,
    val name: String,
    val path: String,
    val description: String,
    val category: String = "All",
    val imageUrl: String? = null
)
