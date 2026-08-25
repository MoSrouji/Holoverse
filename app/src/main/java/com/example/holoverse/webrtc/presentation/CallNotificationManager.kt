package com.example.holoverse.webrtc.presentation

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
import com.example.holoverse.core.utils.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    private val TAG = "CallNotificationManager"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "incoming_calls"
        const val ONGOING_CHANNEL_ID = "active_calls"
        const val NOTIFICATION_ID = 1001
        const val ONGOING_NOTIFICATION_ID = 1002
        const val ACTION_ANSWER = "com.example.holoverse.ACTION_ANSWER_CALL"
        const val ACTION_DECLINE = "com.example.holoverse.ACTION_DECLINE_CALL"
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_CALLER_IMAGE = "caller_image"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = preferenceManager.getRingtone(PreferenceManager.KEY_CALL_RINGTONE)
            
            // Check if user disabled in system
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null && existingChannel.importance == NotificationManager.IMPORTANCE_NONE) {
                // Channel is disabled, but we might still want to create the ongoing one
            } else {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Incoming Calls",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shows notifications for incoming video calls"
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 500, 500)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

                    if (ringtoneUri != null) {
                        setSound(
                            android.net.Uri.parse(ringtoneUri),
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Create ongoing call channel (lower importance to avoid pop-up during call, but visible in drawer)
            val ongoingChannel = NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Active Calls",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows notification when a call is in progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(ongoingChannel)
        }
    }

    fun getOngoingCallNotification(): android.app.Notification {
        createNotificationChannel()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.holoverse.OPEN_ACTIVE_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("HoloVerse Call")
            .setContentText("Call in progress...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun showIncomingCallNotification(callId: String, roomId: String, callerName: String, callerImageUrl: String?) {
        if (!preferenceManager.getNotificationSetting(PreferenceManager.KEY_CALL_NOTIFICATIONS)) {
            Log.d(TAG, "Call notifications disabled by user in-app")
            return
        }

        createNotificationChannel()
        
        // Final check: if system channel is disabled, return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                Log.d(TAG, "Call notification channel is disabled in system settings")
                return
            }
        }
        
        // Use a unique intent action for the full screen intent to avoid collisions
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.holoverse.INCOMING_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALLER_IMAGE, callerImageUrl)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_ANSWER
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALLER_IMAGE, callerImageUrl)
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context, 1, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_DECLINE
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_ROOM_ID, roomId)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context, 2, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Group Call Invite")
            .setContentText("$callerName is inviting you to a group session")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_launcher_foreground, "Decline", declinePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Answer", answerPendingIntent)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

