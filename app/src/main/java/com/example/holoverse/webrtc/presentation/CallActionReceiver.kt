package com.example.holoverse.webrtc.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.holoverse.MainActivity
import com.example.holoverse.webrtc.data.datasource.SignalingClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {
    private val TAG = "CallActionReceiver"

    @Inject
    lateinit var signalingClient: SignalingClient

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val callId = intent.getStringExtra(CallNotificationManager.EXTRA_CALL_ID) ?: return
        Log.d(TAG, "onReceive: action=$action, callId=$callId")

        when (action) {
            CallNotificationManager.ACTION_ANSWER -> {
                // Launch MainActivity with Answer Action
                val callerName = intent.getStringExtra(CallNotificationManager.EXTRA_CALLER_NAME)
                val callerImage = intent.getStringExtra(CallNotificationManager.EXTRA_CALLER_IMAGE)
                
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    this.action = CallNotificationManager.ACTION_ANSWER
                    putExtra(CallNotificationManager.EXTRA_CALL_ID, callId)
                    putExtra(CallNotificationManager.EXTRA_CALLER_NAME, callerName)
                    putExtra(CallNotificationManager.EXTRA_CALLER_IMAGE, callerImage)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
                callNotificationManager.cancelNotification()
            }
            CallNotificationManager.ACTION_DECLINE -> {
                // Clear the call in signaling
                signalingClient.clearCall(callId)
                callNotificationManager.cancelNotification()
            }
        }
    }
}
