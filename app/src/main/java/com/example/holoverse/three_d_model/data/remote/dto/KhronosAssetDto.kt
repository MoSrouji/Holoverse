package com.example.holoverse.three_d_model.data.remote.dto

import com.example.holoverse.three_d_model.domain.model.Model
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KhronosAssetDto(
    val label: String,
    val name: String,
    val screenshot: String?,
    val variants: Map<String, String>,
    val tags: List<String>? = null
)

fun KhronosAssetDto.toModel(): Model? {
    val fileName = variants["glTF-Binary"] ?: return null
    val baseUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/main/Models/"
    val category = tags?.firstOrNull() ?: "Other"
    return Model(
        id = name,
        name = label,
        path = "$baseUrl$name/glTF-Binary/$fileName",
        description = "Khronos Sample Model: $label",
        category = category,
        imageUrl = screenshot?.let { "$baseUrl$name/$it" }
    )
}
