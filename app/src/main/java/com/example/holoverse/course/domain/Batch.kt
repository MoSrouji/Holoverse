package com.example.holoverse.course.domain

import com.google.firebase.Timestamp

data class Batch(
    val id: String = "",
    val courseId: String = "",
    val mentorId: String = "",
    val timeSlot: String = "", // e.g., "Morning", "Afternoon", "Night"
    val capacity: Int = 10,
    val enrolledStudentIds: List<String> = emptyList(),
    val status: BatchStatus = BatchStatus.WAITING,
    val createdAt: Timestamp = Timestamp.now()
)

enum class BatchStatus {
    WAITING,    // Waiting for students to fill up
    ACTIVE,     // Full and lessons are ongoing
    COMPLETED   // All sessions finished
}

data class BatchSession(
    val id: String = "",
    val batchId: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val status: SessionStatus = SessionStatus.PROPOSED,
    val proposedTimes: List<Timestamp> = emptyList(),
    val votes: Map<String, List<String>> = emptyMap() // Map<TimestampIndex, List<StudentId>>
)

enum class SessionStatus {
    PROPOSED,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
