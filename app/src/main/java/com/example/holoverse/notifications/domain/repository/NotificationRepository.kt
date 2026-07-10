package com.example.holoverse.notifications.domain.repository

import com.example.holoverse.notifications.domain.models.Notification
import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendCourseNotificationToFollowers(
        mentorId: String,
        mentorName: String,
        courseId: String,
        courseName: String
    ): Response<Boolean>

    fun getNotifications(userId: String): Flow<Response<List<Notification>>>
    
    suspend fun markAsRead(notificationId: String): Response<Boolean>
}

