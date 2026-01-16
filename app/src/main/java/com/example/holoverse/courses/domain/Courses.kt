package com.example.holoverse.courses.domain

data class Courses(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val duration: String = "",
    val level: String = "",
    val rating: Double = 0.0,
    val numReviews: Int = 0,
    val numEnrolled: Int = 0,
    val instructorId: String = "",
    val instructorName: String = "",
    val instructor: String = "",
    val description: String = "",
    val imageUrl: String = ""
)