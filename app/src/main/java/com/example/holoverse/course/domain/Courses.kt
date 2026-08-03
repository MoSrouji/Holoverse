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
    val sessions: List<CourseSession> = emptyList(),
    val quizzes: List<Quiz> = emptyList()
)

data class CourseSession(
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = ""
)

data class Quiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timeLimitMinutes: Int = 30,
    val questions: List<Question> = emptyList(),
    val imageUrl: String = ""
)

data class Question(
    val id: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val options: List<String> = emptyList(), // Should be exactly 4
    val correctOptionIndices: List<Int> = emptyList() // Support for multiple true answers
)

data class QuizResult(
    val id: String = "",
    val quizId: String = "",
    val userId: String = "",
    val courseId: String = "",
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
