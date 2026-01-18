package com.example.holoverse.auth.domain.entities

sealed interface User {
    val userId: String?
    val fullName: String?
    val email: String?
    val accountType: UserType

    data class Student(
        override val userId: String? = null,
        override val fullName: String? = null,
        override val email: String? = null,
        override val accountType: UserType = UserType.Student,

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

        // Social & Interaction
        val followingTeachers: List<String>? = null,
        val studyGroups: List<String>? = null,
        val peerConnections: List<String>? = null,
        val mentoringStatus: String? = null
    ) : User

    data class Teacher(
        override val userId: String? = null,
        override val fullName: String? = null,
        override val email: String? = null,
        override val accountType: UserType = UserType.Teacher,

        // Personal Information
        val dateOfBirth: String? = null,
        val phoneNumber: String? = null,
        val address: String? = null,
        val gender: String? = null,
        val bio: String? = null,

        // Professional Information
        val yearsOfExperience: String? = null,
        val specialization: TeacherCategory = TeacherCategory.OTHER,
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

        // Statistics & Performance
        val totalStudentsTaught: Int? = null,
        val totalHoursTaught: Float? = null,
        val successRate: Double? = null,
        val reviewsCount: Int? = null,
        val averageRating: Double? = null,

        // Teaching Materials
        val resourcesUploaded: Int? = null,
        val coursesCreated: List<String>? = null
    ) : User
}

enum class UserType {
    Student, Teacher
}

enum class TeacherCategory(val specializations: List<String>) {
    MATHEMATICS(
        listOf(
            "Elementary Mathematics",
            "Algebra Specialist",
            "Geometry Expert",
            "Calculus Expert",
            "Statistics & Probability",
            "Math Competition Coach"
        )
    ),
    SCIENCE(
        listOf(
            "Physics Specialist",
            "Chemistry Expert",
            "Biology Teacher",
            "Environmental Science",
            "Earth Science",
            "AP Science"
        )
    ),
    LANGUAGES(
        listOf(
            "English Language Arts",
            "ESL/EFL Specialist",
            "Foreign Languages",
            "Reading Specialist",
            "Writing Coach"
        )
    ),
    HUMANITIES(
        listOf(
            "History Teacher",
            "Social Studies",
            "Geography",
            "Philosophy",
            "Psychology"
        )
    ),
    TEST_PREP(
        listOf(
            "SAT/ACT Prep Specialist",
            "College Entrance Exams",
            "Graduate Test Prep (GRE/GMAT)",
            "Standardized Test Strategies"
        )
    ),
    COMPUTER_SCIENCE(
        listOf(
            "Computer Science Teacher",
            "Programming Mentor",
            "Web Development",
            "Data Science"
        )
    ),
    ARTS(
        listOf(
            "Music Teacher",
            "Art & Design",
            "Drama/Theater",
            "Creative Writing"
        )
    ),
    SPECIAL_EDUCATION(
        listOf(
            "Learning Disabilities Specialist",
            "Autism Spectrum Support",
            "Dyslexia Intervention",
            "Inclusive Education"
        )
    ),
    BUSINESS(
        listOf(
            "Business Studies",
            "Economics",
            "Accounting",
            "Entrepreneurship"
        )
    ),
    OTHER(
        listOf(
            "Elementary Education",
            "Specialized Tutor",
            "Homework Help",
            "Study Skills"
        )
    );

    companion object {
        fun fromString(name: String): TeacherCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }

        fun getAllCategoryNames(): List<String> {
            return entries.map { it.name }
        }
    }
}
