package com.example.holoverse.ui.spatialTheme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


// --- 1. Data Class for a single particle ---
data class Particle(
    var x: Float,
    var y: Float,
    val initialRadius: Float,
    var currentRadius: Float,
    val angle: Float,
    val speed: Float,
    val color: Color
)

// --- 2. Particle System Helper ---
class ParticleSystem(private val numParticles: Int, private val maxRadius: Float) {
    private val particles = mutableListOf<Particle>()
    private val random = Random(System.currentTimeMillis())

    init {
        // Initialize particles in a sphere distribution
        repeat(numParticles) {
            val angle = random.nextFloat() * 360f
            val radiusFraction = random.nextFloat().let { it * it }
            val radius = radiusFraction * maxRadius

            particles.add(
                Particle(
                    x = 0f,
                    y = 0f,
                    initialRadius = radius,
                    currentRadius = radius,
                    angle = angle,
                    speed = random.nextFloat() * 0.05f + 0.01f,
                    color = Color(0xFFC8A2C8).copy(alpha = random.nextFloat() * 0.7f + 0.3f)
                )
            )
        }
    }

    // Update particle positions
    fun update(center: Offset, time: Float) {
        particles.forEach { p ->
            val displacement = sin(time * p.speed) * (p.initialRadius * 0.1f)
            p.currentRadius = p.initialRadius + displacement

            p.x = center.x + p.currentRadius * cos(Math.toRadians(p.angle.toDouble())).toFloat()
            p.y = center.y + p.currentRadius * sin(Math.toRadians(p.angle.toDouble())).toFloat()
        }
    }

    // Draw all particles
    // Fix: Pass radiusPx here so we don't need 'toPx()' inside this class
    fun draw(drawScope: DrawScope, radiusPx: Float) {
        particles.forEach { p ->
            drawScope.drawCircle(
                color = p.color,
                radius = radiusPx,
                center = Offset(p.x, p.y)
            )
        }
    }
}// --- 3. Composable for the Animated Particle Ball (No change needed here) ---
@Composable
fun ParticleBall(
    modifier: Modifier = Modifier,
    onFlash: () -> Unit
) {
    // ... (ParticleBall logic remains the same, it still calls onFlash) ...
    // Note: The particle ball's internal flash logic will still happen periodically,
    // but the navigation trigger is now managed by the parent HoloIntroScreen.

    // ... (rest of ParticleBall implementation) ...

    // Use the previous ParticleBall implementation here, keeping the flash animation
    val numParticles = 800
    val maxSphereRadius = 150.dp

    val animProgress = rememberInfiniteTransition(label = "flashTransition").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.9f at 2800
                1f at 2900
                0.9f at 3000
            },
            repeatMode = RepeatMode.Restart
        ), label = "animProgress"
    )

    var hasFlashed by remember { mutableStateOf(false) }

    LaunchedEffect(animProgress.value) {
        if (animProgress.value > 0.99f && !hasFlashed) {
            onFlash()
            hasFlashed = true
        } else if (animProgress.value < 0.1f) {
            hasFlashed = false
        }
    }

    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        time.animateTo(
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    val particleSystem = remember { ParticleSystem(numParticles, maxSphereRadius.value) }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val particleRadiusPx = 1.5.dp.toPx()

        particleSystem.update(center, time.value)

        val flashScale = 1f + (animProgress.value * 0.15f)

        scale(scaleX = flashScale, scaleY = flashScale, pivot = center) {
            particleSystem.draw(this, particleRadiusPx)
        }
    }
}


// --- 4. Intro Screen Composable (NEW NAVIGATION LOGIC) ---
@Composable
fun HoloIntroScreen(
    onNavigationComplete: () -> Unit
) {
    var showText by remember { mutableStateOf(false) }

    // 🔥 This LaunchedEffect handles the 3-second delay and navigation.
    LaunchedEffect(Unit) {
        // Wait for 3000 milliseconds (3 seconds)
        delay(3000L)

        // After delay, trigger the text display and then immediately navigate
        showText = true

        // An optional, shorter delay to allow the text to be seen briefly
        delay(500L)

        onNavigationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            //.background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // The ParticleBall still animates, but its flash no longer controls the state change.

        SpatialBackground()

        // Show "Holoverse" only when triggered by the timer
        if (showText) {
            Text(
                text = "Holoverse",
                style = TextStyle(
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8A2C8).copy(alpha = 0.9f)
                )
            )
        }
        else{
            ParticleBall(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                onFlash = {
                    // We keep the onFlash call but it's now ignored for navigation,
                    // as navigation is controlled by the timer above.
                }
            )
        }
    }

}


// --- 4. Final Screen Composable ---
@Composable
fun HoloverseScreen() {
    var showText by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showText) {
            Text(
                text = "Holoverse",
                style = TextStyle(
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8A2C8).copy(alpha = 0.9f)
                )
            )
        } else {
            ParticleBall(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                onFlash = {
                    showText = true
                }
            )
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
   HoloIntroScreen(
       onNavigationComplete = {}
   )
}