package com.example.holoverse.notifications.domain.repository

import com.example.holoverse.notifications.domain.models.Notification
import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.Flow

enum class BroadcastTarget { ALL, STUDENTS, MENTORS }

interface NotificationRepository {
    suspend fun sendCourseNotificationToFollowers(
        mentorId: String,
        mentorName: String,
        courseId: String,
        courseName: String
    ): Response<Boolean>

    fun getNotifications(userId: String): Flow<Response<List<Notification>>>
    
    suspend fun markAsRead(notificationId: String): Response<Boolean>

    suspend fun sendBroadcastNotification(
        title: String,
        body: String,
        target: BroadcastTarget
    ): Response<Boolean>

    suspend fun sendMentorBatchNotification(
        mentorId: String,
        courseName: String,
        timeSlot: String
    ): Response<Boolean>
}

