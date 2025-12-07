package com.example.holoverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.holoverse.navigation.AppNavHost
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.theme.HoloverseTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: AppNavigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HoloverseTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //SpatialBackground()
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        navigator = navigator
                    )

                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            AnimatedSplashScreen(navController = navController)
        }
        composable("main") {
            MainScreen()
        }
    }
}

@Composable
fun AnimatedSplashScreen(navController: NavController) {
    // Animation states
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    // Background color
    val backgroundColor = Color(0xFF1877F2) // Facebook blue color

    LaunchedEffect(Unit) {
        // Start all animations
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        delay(500) // Wait a bit before showing text

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        delay(1000) // Keep splash screen visible for 1 second

        // Scale down and fade out
        scale.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(durationMillis = 400)
        )

        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400)
        )

        textAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400)
        )

        // Navigate to main screen
        navController.navigate("main") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Animated Logo/Circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .alpha(alpha.value)
                .background(Color.White, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "f",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        }

        // App Name (appears after logo)
        Text(
            text = "MySocialApp",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(textAlpha.value)
                .padding(bottom = 100.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to Main Screen!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun MySplashAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MySplashAppTheme {
        MainScreen()
    }
}
