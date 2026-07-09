package com.example.holoverse.auth.domain.entities

import com.example.holoverse.core.domain.model.AppCategory
import kotlinx.serialization.Serializable

@Serializable
sealed interface User {
    val userId: String?
    val fullName: String?
    val email: String?
    val accountType: UserType
    val fcmToken: String?

    @Serializable
    data class Student(
        override val userId: String? = null,
        override val fullName: String? = null,
        override val email: String? = null,
        override val accountType: UserType = UserType.Student,
        override val fcmToken: String? = null,

        // Personal Information
        val profileImageUrl: String? = null,
        val dateOfBirth: String? = null,
        val phoneNumber: String? = null,
        val address: String? = null,
        val gender: String? = null,

        // Academic Information
        val currentGradeLevel: String? = null,
        val currentYear: Int? = null,
        val gpa: Double? = null,
        val academicStanding: String? = null,
        val expectedGraduationYear: Int? = null,
        val minorSubjects: List<String>? = null,
        val academicInterests: List<String>? = null,

        // Educational Background
        val universityName: String? = null,
        val faculty: String? = null,
        val department: String? = null,

        // Learning Preferences
        val preferredLearningTime: String? = null,
        val studyEnvironment: String? = null,
        val languageProficiency: Map<String, String>? = null,
        val favouriteSubjects: List<String>? = null,

        // Course & Enrollment Information
        val enrolledCourses: List<String>? = null,
        val completedCourses: List<String>? = null,
        val currentCourses: List<String>? = null,
        val savedCourses: List<String>? = null,

        // Social & Interaction
        val following: List<String>? = null,
        val followingCount: Int? = 0,
        val studyGroups: List<String>? = null,
        val peerConnections: List<String>? = null,
        val mentoringStatus: String? = null,
        val createdAt: Long? = null
    ) : User

    @Serializable
    data class Mentor(
        override val userId: String? = null,
        override val fullName: String? = null,
        override val email: String? = null,
        override val accountType: UserType = UserType.Mentor,
        override val fcmToken: String? = null,

        // Personal Information
        val profileImageUrl: String? = null,
        val dateOfBirth: String? = null,
        val phoneNumber: String? = null,
        val address: String? = null,
        val gender: String? = null,
        val bio: String? = null,

        // Professional Information
        val yearsOfExperience: String? = null,
        val specialization: AppCategory = AppCategory.OTHER,
        val subjects: List<String>? = null,
        val certifications: String? = null,
        val languagesSpoken: List<String>? = null,
        val hourlyRate: Double? = null,

        // Educational Background
        val universityAttended: String? = null,
        val graduationYear: String? = null,
        val additionalQualifications: String? = null,

        // Preferences
        val preferredStudentLevel: String? = null,
        val maxStudentsPerSession: String? = null,
        val preferredLearningTime: String? = null,

        // Social & Interaction
        val followers: List<String>? = null,
        val following: List<String>? = null,
        val followersCount: Int? = 0,
        val followingCount: Int? = 0,

        // Statistics & Performance
        val totalStudentsTaught: Int? = null,
        val totalHoursTaught: Float? = null,
        val successRate: Double? = null,
        val reviewsCount: Int? = null,
        val averageRating: Double? = null,

        // Teaching Materials
        val resourcesUploaded: Int? = null,
        val coursesCreated: List<String>? = null,

        // Enrollment Information (for mentors taking courses)
        val enrolledCourses: List<String>? = null,
        val completedCourses: List<String>? = null,
        val currentCourses: List<String>? = null,
        val savedCourses: List<String>? = null,
        val createdAt: Long? = null
    ) : User
}

@Serializable
enum class UserType {
    Student, Mentor
}
