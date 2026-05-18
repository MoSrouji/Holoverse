package com.example.holoverse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.navigation.AppNavHost
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.theme.HoloverseTheme
import com.example.holoverse.utils.LanguageManager
import com.example.holoverse.utils.SplashViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.Color as ComposeColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: AppNavigator
    
    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var authRepository: AuthRepository
    
    private val splashViewModel: SplashViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchAndStoreFcmToken()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        languageManager.applyLanguage()
        askNotificationPermission()

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
            val isLoading by splashViewModel.isLoading.collectAsStateWithLifecycle()

            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    fetchAndStoreFcmToken()
                }
            }

            HoloverseTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ComposeColor.Transparent
                ) {
                    if (!isLoading) {
                        val navController = rememberNavController()

                            AppNavHost(
                                navController = navController,
                                navigator = navigator,
                                isLoggedIn = currentUser != null,
                                darkTheme = darkTheme
                            )


                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                fetchAndStoreFcmToken()
            }
        } else {
            fetchAndStoreFcmToken()
        }
    }

    private fun fetchAndStoreFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                CoroutineScope(Dispatchers.IO).launch {
                    authRepository.updateFcmToken(token)
                }
            }
        }
    }
}
