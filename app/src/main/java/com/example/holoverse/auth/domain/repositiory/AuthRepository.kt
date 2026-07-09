package com.example.holoverse.auth.domain.repositiory

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun firebaseSignUp(
        userDto: User,
        password: String

    ): Flow<Response<Boolean>>

    suspend fun firebaseSignIn(
        email: String,
        password: String
    ): Flow<Response<Boolean>>

    suspend fun firebaseSignOut(): Flow<Response<Boolean>>
    suspend fun updateMentorProfile(mentor: User.Mentor): Flow<Response<Boolean>>
    suspend fun updateStudentProfile(student: User.Student): Flow<Response<Boolean>>
    suspend fun updateEmail(newEmail: String): Flow<Response<Boolean>>
    suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Response<Boolean>>
    suspend fun getCurrentUser(): User?
    fun getCachedUser(): User?
    suspend fun updateUser(user: User?, newEmail: String? = null): Boolean
    suspend fun updateFcmToken(token: String): Response<Boolean>
    suspend fun getFcmToken(userId: String): String?

    suspend fun followMentor(followerId: String, mentorId: String): Response<Boolean>
    suspend fun unfollowMentor(followerId: String, mentorId: String): Response<Boolean>
    suspend fun isFollowing(followerId: String, mentorId: String): Boolean

    suspend fun enrollInCourse(userId: String, courseId: String, instructorId: String): Response<Boolean>
    suspend fun toggleSaveCourse(userId: String, courseId: String): Response<Boolean>
    suspend fun addCourseToMentor(mentorId: String, courseId: String): Response<Boolean>
}
