package com.example.holoverse.threedmodel.domain.model

data class Model(
    val id: String,
    val name: String,
    val path: String,
    val description: String,
    val category: String = "Other",
    val imageUrl: String? = null
)
