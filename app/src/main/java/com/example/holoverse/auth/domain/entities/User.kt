package com.example.holoverse.auth.domain.entities

import android.R
import kotlinx.coroutines.flow.Flow

open class User(

    open val userId: String? = null,
    open val fullName: String? = null,
    open val email: String? = null,
    open val accountType: UserType
)

data class Student(
    override val userId: String? = null,
    override val fullName: String? = null,
    override val email: String? = null,

    // Personal Information
    val profileImageUrl: String? = null,
    val dateOfBirth: String? = null, // Consider using LocalDate
    val phoneNumber: String? = null,
    val address: String? = null,
    val gender: String? = null,

    // Academic Information
    val currentGradeLevel: String? = null, // Freshman, Sophomore, etc.
    val currentYear: Int? = null,
    val gpa: Double? = null,
    val academicStanding: String? = null, // Good, Probation, etc.
    val expectedGraduationYear: Int? = null,
    val minorSubjects: List<String>? = null,
    val academicInterests: List<String>? = null,

    // Educational Background
    val universityName: String? = null,
    val faculty: String? = null,
    val department: String? = null,

    // Learning Preferences
    val preferredLearningTime: String? = null, // Morning, Evening, etc.
    val studyEnvironment: String? = null, // Quiet, Group, etc.
    val languageProficiency: Map<String, String>? = null, // Language -> Level
    val favouriteSubjects: List<String>? = null,


    // Course & Enrollment Information
    val enrolledCourses: List<String>? = null,
    val completedCourses: List<String>? = null,
    val currentCourses: List<String>? = null,


    // Social & Interaction
    val followingTeachers: List<String>? = null,
    val studyGroups: List<String>? = null,
    val peerConnections: List<String>? = null,
    val mentoringStatus: String? = null, // Seeking, Has Mentor, Mentor


) : User(accountType = UserType.Student)


data class Teacher(
    override var userId: String? = null,
    override var fullName: String? = null,
    override var email: String? = null,


    // Personal Information
    // val profileImageUrl: String? = null,
    var dateOfBirth: String? = null, // or use LocalDate
    var phoneNumber: String? = null,
    var address: String? = null,
    var gender: String? = null,
    var bio: String? = null,


    // Professional Information
    var yearsOfExperience: Int? = null,
    var specialization: String? = null,
    var subjects: List<String>? = null,
    var certifications: List<String>? = null,
    var languagesSpoken: List<String>? = null,
    var hourlyRate: Double? = null,

    // Educational Background
    var universityAttended: String? = null,
    var graduationYear: String? = null,
    var additionalQualifications: String? = null,

    // Preferences
    var preferredStudentLevel: String? = null, // Beginner, Intermediate, Advanced
    var maxStudentsPerSession: String? = null,
    var preferredLearningTime: String? = null, // Morning, Evening, etc.


    // Statistics & Performance
    val totalStudentsTaught: Int? = null,
    val totalHoursTaught: Float? = null,
    val successRate: Double? = null,
    val reviewsCount: Int? = null,
    val averageRating: Double? = null,


    // Teaching Materials
    val resourcesUploaded: Int? = null,
    val coursesCreated: List<String>? = null,

    ) : User(accountType = UserType.Teacher)

enum class UserType {
    Student, Teacher
}

enum class TeacherCategory(string: String) {
    MATHEMATICS("MATHEMATICS"), SCIENCE("SCIENCE"), LANGUAGES("LANGUAGES"), HUMANITIES("HUMANITIES"),
    TEST_PREP("TEST_PREP"), COMPUTER_SCIENCE("COMPUTER_SCIENCE"), ARTS("ARTS"), SPECIAL_EDUCATION("SPECIAL_EDUCATION"),
    BUSINESS("BUSINESS"), OTHER("OTHER")
}

fun getAllCategoryNames(): List<String> {
    return TeacherCategory.entries.map { it.name }
}

fun getSpecialization(string: String): TeacherCategory {
    return when (string) {
        "MATHEMATICS" -> TeacherCategory.MATHEMATICS
        "SCIENCE" -> TeacherCategory.SCIENCE
        "LANGUAGES" -> TeacherCategory.LANGUAGES
        "HUMANITIES" -> TeacherCategory.HUMANITIES
        "TEST_PREP" -> TeacherCategory.TEST_PREP
        "COMPUTER_SCIENCE" -> TeacherCategory.COMPUTER_SCIENCE
        "ARTS" -> TeacherCategory.ARTS
        "SPECIAL_EDUCATION" -> TeacherCategory.SPECIAL_EDUCATION
        "BUSINESS" -> TeacherCategory.BUSINESS
        "OTHER" -> TeacherCategory.OTHER
        else -> {
            TeacherCategory.OTHER
        }

    }
}


// Mapping of specializations to subjects
object TeacherSpecializationMapper {
    // Get all specializations by category
    fun getSpecializationsByCategory(category: TeacherCategory): List<String> {
        return when (category) {
            TeacherCategory.MATHEMATICS -> listOf(
                "Elementary Mathematics",
                "Algebra Specialist",
                "Geometry Expert",
                "Calculus Expert",
                "Statistics & Probability",
                "Math Competition Coach"
            )

            TeacherCategory.SCIENCE -> listOf(
                "Physics Specialist",
                "Chemistry Expert",
                "Biology Teacher",
                "Environmental Science",
                "Earth Science",
                "AP Science"
            )

            TeacherCategory.LANGUAGES -> listOf(
                "English Language Arts",
                "ESL/EFL Specialist",
                "Foreign Languages",
                "Reading Specialist",
                "Writing Coach"
            )

            TeacherCategory.HUMANITIES -> listOf(
                "History Teacher",
                "Social Studies",
                "Geography",
                "Philosophy",
                "Psychology"
            )

            TeacherCategory.TEST_PREP -> listOf(
                "SAT/ACT Prep Specialist",
                "College Entrance Exams",
                "Graduate Test Prep (GRE/GMAT)",
                "Standardized Test Strategies"
            )

            TeacherCategory.COMPUTER_SCIENCE -> listOf(
                "Computer Science Teacher",
                "Programming Mentor",
                "Web Development",
                "Data Science"
            )

            TeacherCategory.ARTS -> listOf(
                "Music Teacher",
                "Art & Design",
                "Drama/Theater",
                "Creative Writing"
            )

            TeacherCategory.SPECIAL_EDUCATION -> listOf(
                "Learning Disabilities Specialist",
                "Autism Spectrum Support",
                "Dyslexia Intervention",
                "Inclusive Education"
            )

            TeacherCategory.BUSINESS -> listOf(
                "Business Studies",
                "Economics",
                "Accounting",
                "Entrepreneurship"
            )

            TeacherCategory.OTHER -> listOf(
                "Elementary Education",
                "Specialized Tutor",
                "Homework Help",
                "Study Skills"
            )
        }
    }


}


//
//
//data class Teacher(
//
//
//    // Availability & Schedule
//    val availabilityStatus: String? = null, // Online, Offline, Busy, etc.
//    val workingHours: Map<String, List<String>>? = null, // Day -> Time slots
//    val timezone: String? = null,
//
//
//    // Platform & Technical
//    val isVerified: Boolean? = null,
//    val joinDate: String? = null, // or use LocalDate
//    val lastActive: String? = null, // or use Instant/LocalDateTime
//    val preferredContactMethod: String? = null,
//    val socialMediaLinks: Map<String, String>? = null, // Platform -> URL
//
//    // Teaching Materials
//    val resourcesUploaded: Int? = null,
//    val coursesCreated: List<String>? = null,
//
//    // Payment & Financial
//    val paymentMethod: String? = null,
//    val bankAccountDetails: Map<String, String>? = null,
//
//    // Preferences
//    val preferredStudentLevel: String? = null, // Beginner, Intermediate, Advanced
//    val maxStudentsPerSession: Int? = null,
//    val teachingPlatform: String? = null, // Online, In-person, Hybrid
//    val meetingPreferences: List<String>? = null // Video, Audio, Chat, etc.
//) : User(accountType = UserType.Teacher)


//
//data class Student(
//    override val userId: String? = null,
//    override val fullName: String? = null,
//    override val email: String? = null,
//    val favouriteTopic: List<String> = emptyList(),
//    val studyMajor: String? = null,
//
//
//
//
//
//
//    // Performance & Progress
//    val averageScore: Double? = null,
//    val assignmentCompletionRate: Double? = null,
//    val quizScores: Map<String, Double>? = null, // Quiz Name -> Score
//    val examResults: Map<String, Double>? = null, // Exam Name -> Score
//    val learningGoals: List<String>? = null,
//    val achievements: List<String>? = null,
//
//    // Extracurricular & Skills
//    val clubsMemberships: List<String>? = null,
//    val sportsActivities: List<String>? = null,
//    val hobbies: List<String>? = null,
//    val skills: List<String>? = null,
//    val certifications: List<String>? = null,
//    val awards: List<String>? = null,
//
//    // Career & Professional
//    val careerGoals: String? = null,
//    val internshipStatus: String? = null, // Looking, Applied, Secured
//    val jobInterests: List<String>? = null,
//    val linkedInProfile: String? = null,
//    val portfolioUrl: String? = null,
//
//    // Financial Information
//    val tuitionStatus: String? = null, // Paid, Pending, Scholarship
//    val scholarshipType: String? = null,
//    val financialAidStatus: String? = null,
//
//    // Platform & Technical
//    val isEmailVerified: Boolean? = false,
//    val isProfileComplete: Boolean? = false,
//    val accountCreationDate: String? = null,
//    val lastLoginDate: String? = null,
//    val lastActivity: String? = null,
//    val deviceTokens: List<String>? = null, // For push notifications
//    val notificationPreferences: Map<String, Boolean>? = null,
//    val privacySettings: Map<String, Boolean>? = null,
//
//
//    // Progress Tracking
//    val studyHoursPerWeek: Int? = null,
//    val assignmentSubmissionRate: Double? = null,
//    val participationScore: Double? = null,
//    val improvementAreas: List<String>? = null,
//    val strengths: List<String>? = null
//) : User(accountType = UserType.Student)
