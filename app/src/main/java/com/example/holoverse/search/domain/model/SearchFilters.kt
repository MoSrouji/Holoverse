package com.example.holoverse.search.domain.model

data class CourseFilters(
    val query: String? = null,
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val level: String? = null,
    val minRating: Double? = null,
    val duration: String? = null
)

data class MentorFilters(
    val query: String? = null,
    val specialization: String? = null,
    val subjects: List<String>? = null,
    val minHourlyRate: Double? = null,
    val maxHourlyRate: Double? = null,
    val minRating: Double? = null,
    val yearsOfExperience: String? = null
)