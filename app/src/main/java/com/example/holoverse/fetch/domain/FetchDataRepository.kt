package com.example.holoverse.fetch.domain

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.BoostedCourse
import com.example.holoverse.courses.domain.Courses

interface FetchDataRepository {

    suspend fun fetchCourses(forceRefresh: Boolean = false): List<Courses>
    suspend fun fetchMentors(forceRefresh: Boolean = false): List<User.Mentor>
    suspend fun fetchMentorById(mentorId: String): User.Mentor?
    suspend fun fetchAds()

    suspend fun fetchBoostedCourses(): List<BoostedCourse>
    suspend fun cleanupExpiredBoosts()
}
