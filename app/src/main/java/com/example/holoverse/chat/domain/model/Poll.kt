package com.example.holoverse.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Poll(
    val question: String = "",
    val options: List<String> = emptyList(),
    val votes: Map<String, List<String>> = emptyMap(), // Option index (as string) -> List of userIds
    val isClosed: Boolean = false
)
