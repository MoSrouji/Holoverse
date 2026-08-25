package com.example.holoverse.fetch.domain

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.google.firebase.firestore.DocumentSnapshot

interface FetchDataRepository {

    suspend fun fetchCourses(
        forceRefresh: Boolean = false,
        limit: Long = 20,
        lastVisible: DocumentSnapshot? = null
    ): Pair<List<Courses>, DocumentSnapshot?>

    suspend fun fetchMentors(
        forceRefresh: Boolean = false,
        limit: Long = 20,
        lastVisible: DocumentSnapshot? = null
    ): Pair<List<User.Mentor>, DocumentSnapshot?>

    suspend fun fetchRecommendedCourses(
        categories: List<String>,
        limit: Int = 10
    ): List<Courses>

    suspend fun fetchRecommendedMentors(
        specializations: List<String>,
        limit: Int = 10
    ): List<User.Mentor>

    suspend fun fetchMentorById(mentorId: String): User.Mentor?
    suspend fun fetchAds()

    suspend fun fetchBoostedCourses(): List<BoostedCourse>
    suspend fun cleanupExpiredBoosts()

    suspend fun fetchStudentsByIds(studentIds: List<String>): List<User.Student>
    fun clearCache()
}

