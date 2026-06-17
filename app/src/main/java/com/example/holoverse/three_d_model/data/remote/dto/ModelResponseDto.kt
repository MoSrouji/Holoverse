package com.example.holoverse.three_d_model.data.remote.dto

import com.example.holoverse.three_d_model.domain.model.Model
import com.squareup.moshi.JsonClass

import java.util.Locale

@JsonClass(generateAdapter = true)
data class ModelResponseDto(
    val baseUrl: String,
    val models: Map<String, String>
)

fun ModelResponseDto.toModels(): List<Model> {
    return models.map { (key, path) ->
        Model(
            id = key,
            name = key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            path = "$baseUrl/$path",
            description = "3D Model: ${key.replace("_", " ")}",
            category = path.substringBefore("/", "Other")
        )
    }
}
