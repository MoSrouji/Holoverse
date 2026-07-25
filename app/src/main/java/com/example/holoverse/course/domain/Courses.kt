package com.example.holoverse.course.domain

import com.example.holoverse.core.domain.model.AppCategory

data class Courses(
    val id: String = "",
    val name: String = "",
    val category: AppCategory = AppCategory.OTHER,
    val specialization: String = "",
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
    val language: String = "",
    val imageUrl: String = "",
    val completionRate: Double = 0.0,
    val averageProgress: Double = 0.0,
    val sessions: List<CourseSession> = emptyList()
)

data class CourseSession(
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = ""
)
