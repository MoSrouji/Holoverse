package com.example.holoverse.material.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String = "",
    val name: String = "",
    val type: MaterialType = MaterialType.MODEL,
    val url: String = "",
    val thumbnailUrl: String? = null,
    val description: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

enum class MaterialType {
    PDF, MODEL, PHOTO, VIDEO
}
