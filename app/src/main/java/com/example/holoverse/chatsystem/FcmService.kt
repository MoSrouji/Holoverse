package com.example.holoverse.chatsystem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.holoverse.MainActivity
import com.example.holoverse.R
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.utils.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.example.holoverse.webrtc.presentation.CallNotificationManager
import com.example.holoverse.notifications.presentation.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    private val TAG = "FcmService"

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.e(TAG, "!!! FCM MESSAGE RECEIVED !!!")
        Log.e(TAG, "Data: ${message.data}")
        Log.e(TAG, "Notification: ${message.notification?.title} / ${message.notification?.body}")
        
        val type = message.data["type"]
        val title = message.data["title"] ?: message.notification?.title
        val body = message.data["body"] ?: message.notification?.body
        val chatId = message.data["chatId"]
        val courseId = message.data["courseId"]
        
        Log.d(TAG, "onMessageReceived: type=$type, title=$title, chatId=$chatId, courseId=$courseId")

        if (type == "call") {
            val callId = message.data["callId"] ?: chatId ?: return
            val callerName = message.data["callerName"] ?: "Someone"
            val callerImage = message.data["callerImage"]
            Log.d(TAG, "Triggering call notification: callId=$callId, caller=$callerName")
            callNotificationManager.showIncomingCallNotification(callId, callerName, callerImage)
        } else if (type == "course_created") {
            NotificationHelper.showNotification(this, title ?: "New Course", body ?: "A new course is available", courseId, preferenceManager = preferenceManager)
        } else if (title != null || body != null) {
            Log.d(TAG, "Triggering chat notification")
            NotificationHelper.showNotification(this, title ?: "New Message", body ?: "", null, chatId, preferenceManager = preferenceManager)
        } else {
            Log.w(TAG, "Received message with no type, title or body")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
