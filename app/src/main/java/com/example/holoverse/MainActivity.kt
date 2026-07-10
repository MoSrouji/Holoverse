package com.example.holoverse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavHost
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.core.ui.theme.HoloverseTheme
import com.example.holoverse.core.utils.LanguageManager
import com.example.holoverse.core.utils.SplashViewModel
import com.example.holoverse.webrtc.data.datasource.SignalingClient
import com.example.holoverse.webrtc.presentation.CallNotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.compose.ui.graphics.Color as ComposeColor

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    @Inject
    lateinit var signalingClient: SignalingClient

    @Inject
    lateinit var sessionManager: com.example.holoverse.webrtc.data.datasource.WebRtcSessionManager

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    private var callObservationJob: Job? = null

    @Inject
    lateinit var navigator: AppNavigator

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestore: FirebaseFirestore


    @Inject
    lateinit var notificationRepo: com.example.holoverse.notifications.domain.repository.NotificationRepository

    private var notificationObservationJob: Job? = null

    private val splashViewModel: SplashViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchAndStoreFcmToken()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
//

        super.onCreate(savedInstanceState)

        handleIntent(intent)


        languageManager.applyLanguage()
        askNotificationPermission()

//        lifecycleScope.launch {
//            DummyDataPopulator(firestore).populateData()
//        }


        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashViewModel.isLoading.value
            }
        }
        setContent {
            val themeMode by splashViewModel.themeMode.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val currentUser by splashViewModel.currentUser.collectAsStateWithLifecycle()
            val isProfileComplete by splashViewModel.isProfileComplete.collectAsStateWithLifecycle()
            val isLoading by splashViewModel.isLoading.collectAsStateWithLifecycle()

            val isLoggedIn = currentUser != null && isProfileComplete

            // Debug log to trace identity changes
            LaunchedEffect(isLoggedIn) {
                Log.d("MainActivity", "Identity change detected: isLoggedIn=$isLoggedIn")
            }

            // Use a stable key for the entire App content to force a full reset on auth change
            key(isLoggedIn) {
                HoloverseTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = ComposeColor.Transparent
                    ) {
                        if (!isLoading) {
                            AppNavHost(
                                navigator = navigator,
                                isLoggedIn = isLoggedIn,
                                darkTheme = darkTheme
                            )
                        }
                    }
                }
            }

            LaunchedEffect(currentUser) {
                currentUser?.userId?.let { userId ->
                    observeIncomingCalls(userId)
                    observeNotifications(userId)
                } ?: run {
                    callObservationJob?.cancel()
                    notificationObservationJob?.cancel()
                }
            }
        }
    }

    private fun observeNotifications(userId: String) {
        Log.d(TAG, "observeNotifications: Starting observer for $userId")
        notificationObservationJob?.cancel()
        notificationObservationJob = lifecycleScope.launch {
            notificationRepo.getNotifications(userId).collectLatest { response ->
                Log.d(TAG, "observeNotifications: Received response: $response")
                // System notifications are now handled via FCM for consistency with messaging/calling.
            }
        }
    }

    private fun observeIncomingCalls(userId: String) {
        callObservationJob?.cancel()
        callObservationJob = lifecycleScope.launch {
            Log.d(TAG, "observeIncomingCalls: Listening for calls for $userId")
            signalingClient.observeGlobalCalls(userId).collectLatest { callId ->
                Log.d(TAG, "In-app call detected: $callId")
                callNotificationManager.showIncomingCallNotification(
                    callId = callId,
                    callerName = "Someone",
                    callerImageUrl = null
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val chatId = intent?.getStringExtra("chatId")
        val courseId = intent?.getStringExtra("courseId")
        val callId = intent?.getStringExtra(CallNotificationManager.EXTRA_CALL_ID)
        val callerName = intent?.getStringExtra(CallNotificationManager.EXTRA_CALLER_NAME)
        val callerImage = intent?.getStringExtra(CallNotificationManager.EXTRA_CALLER_IMAGE)
        val action = intent?.action

        Log.d(
            TAG,
            "handleIntent: action=$action, callId=$callId, chatId=$chatId, courseId=$courseId"
        )

        if (action == CallNotificationManager.ACTION_ANSWER && callId != null) {
            Log.d(TAG, "Navigating to VideoCall (Answering)")
            navigator.navigateTo(AppDestination.VideoCall(callId, isOffer = false))
        } else if (callId != null) {
            Log.d(TAG, "Navigating to IncomingCall")
            navigator.navigateTo(
                AppDestination.IncomingCall(
                    callId = callId,
                    callerName = callerName ?: "Someone",
                    callerImageUrl = callerImage
                )
            )
        } else if (chatId != null) {
            Log.d(TAG, "Navigating to ChatScreen")
            navigator.navigateTo(AppDestination.ChatScreen(mentorId = chatId))
        } else if (courseId != null) {
            Log.d(TAG, "Navigating to CourseDetail")
            navigator.navigateTo(AppDestination.CourseDetail(courseId = courseId))
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "askNotificationPermission: Requesting permission")
                requestPermissionLauncher.launch(permission)
            } else {
                Log.d(TAG, "askNotificationPermission: Permission already granted")
                fetchAndStoreFcmToken()
            }
        } else {
            fetchAndStoreFcmToken()
        }
    }

    private fun fetchAndStoreFcmToken() {
        lifecycleScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token: $token")
                authRepository.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.disconnect()
        callObservationJob?.cancel()
        notificationObservationJob?.cancel()
    }
}

