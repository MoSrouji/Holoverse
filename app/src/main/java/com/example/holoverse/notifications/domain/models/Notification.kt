package com.example.holoverse.notifications.domain.models

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "", // e.g., "course_created"
    val courseId: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    @get:JvmName("isRead")
    val isRead: Boolean = false,
    val senderName: String = "",
    val senderImageUrl: String? = null
)
