package com.example.holoverse.notifications.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.holoverse.MainActivity
import com.example.holoverse.R
import com.example.holoverse.utils.PreferenceManager

object NotificationHelper {
    private const val CHANNEL_ID_MESSAGES = "chat_notifications"
    private const val CHANNEL_ID_TEACHERS = "teacher_notifications"

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        courseId: String?,
        chatId: String? = null,
        preferenceManager: PreferenceManager? = null
    ) {
        if (preferenceManager != null) {
            val isMessage = chatId != null
            val isCourse = courseId != null

            if (isMessage && !preferenceManager.getNotificationSetting(PreferenceManager.KEY_MESSAGE_NOTIFICATIONS)) {
                Log.d("NotificationHelper", "Message notifications disabled by user in-app")
                return
            }
            if (isCourse && !preferenceManager.getNotificationSetting(PreferenceManager.KEY_TEACHER_NOTIFICATIONS)) {
                Log.d("NotificationHelper", "Teacher notifications disabled by user in-app")
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isMessage = chatId != null
        val channelId = if (isMessage) CHANNEL_ID_MESSAGES else CHANNEL_ID_TEACHERS
        val channelName = if (isMessage) "Messages" else "Teacher Updates"
        val ringtoneUri = if (isMessage) {
            preferenceManager?.getRingtone(PreferenceManager.KEY_MESSAGE_RINGTONE)
        } else {
            null // Use default for teachers
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if user disabled this channel in system settings
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel != null && existingChannel.importance == NotificationManager.IMPORTANCE_NONE) {
                Log.d("NotificationHelper", "Notification channel $channelId is disabled in system settings")
                return
            }

            // Always create/update channel to ensure it exists
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
                if (ringtoneUri != null) {
                    setSound(
                        android.net.Uri.parse(ringtoneUri),
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        Log.d("NotificationHelper", "Showing notification: $title")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (courseId != null) {
                putExtra("courseId", courseId)
            }
            if (chatId != null) {
                putExtra("chatId", chatId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        if (ringtoneUri != null) {
            notificationBuilder.setSound(android.net.Uri.parse(ringtoneUri))
        } else {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
