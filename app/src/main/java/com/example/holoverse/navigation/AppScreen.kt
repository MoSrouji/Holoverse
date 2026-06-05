package com.example.holoverse.navigation

import com.example.holoverse.core.domain.model.AppCategory
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed interface AppDestination : NavKey {
    @Serializable
    data object AuthGraph : AppDestination

    @Serializable
    data object HomeGraph : AppDestination

    @Serializable
    data object SubGraph : AppDestination

    @Serializable
    data object HoloIntro : AppDestination

    @Serializable
    data object Login : AppDestination

    @Serializable
    data object SignUp : AppDestination

    @Serializable
    data object SignUpTeacherProfile : AppDestination

    @Serializable
    data object SignUpTeacherProfessional : AppDestination

    @Serializable
    data object SignUpStudentProfile : AppDestination

    @Serializable
    data object SignUpStudentPreference : AppDestination

    @Serializable
    data object HomeScreen : AppDestination

    @Serializable
    data object Profile : AppDestination

    @Serializable
    data object EditProfile : AppDestination

    @Serializable
    data object Category : AppDestination

    @Serializable
    data class CategoryCourses(val category: AppCategory) : AppDestination

    @Serializable
    data class Mentor(val mentorId: String = "") : AppDestination

    @Serializable
    data class MentorProfile(val mentorId: String) : AppDestination

    @Serializable
    data object PopularCourses : AppDestination

    @Serializable
    data object Recommended : AppDestination

    @Serializable
    data object RecommendedMentors : AppDestination

    @Serializable
    data object TopMentors : AppDestination

    @Serializable
    data class Search(val triggerVoice: Boolean = false) : AppDestination

    @Serializable
    data object CreateCourse : AppDestination

    @Serializable
    data object MentorAnalysis : AppDestination

    @Serializable
    data object Transactions : AppDestination

    @Serializable
    data object ChatList : AppDestination

    @Serializable
    data class ChatScreen(val mentorId: String? = null) : AppDestination

    @Serializable
    data object TermsAndConditions : AppDestination

    @Serializable
    data object InviteFriends : AppDestination

    @Serializable
    data class CourseDetail(val courseId: String) : AppDestination

    @Serializable
    data object ArScreen : AppDestination

    @Serializable
    data object ViewerScreen : AppDestination

    @Serializable
    data object GalleryScreen : AppDestination

    @Serializable
    data object ModelGraph : AppDestination
}