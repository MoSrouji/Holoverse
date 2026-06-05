package com.example.holoverse.ui.teacherPart.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.holoverse.courses.domain.AdCardStyle
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import com.example.holoverse.ui.theme.IbarraNovaFont

@Composable
fun BoostConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Boost Course Visibility?", fontFamily = IbarraNovaFont, fontWeight = FontWeight.Bold) },
        text = { Text("Reach more students by boosting your course to the top of the home screen.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
            ) {
                Text("Yes, Boost it!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, thanks")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AdCardSelectionDialog(
    course: Courses,
    onDismiss: () -> Unit,
    onStyleSelected: (AdCardStyle) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(AdCardStyle.STYLE_1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Choose Ad Style",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = IbarraNovaFont,
                    fontWeight = FontWeight.Bold
                )

                AdCardPreview(course, AdCardStyle.STYLE_1, selectedStyle == AdCardStyle.STYLE_1) {
                    selectedStyle = AdCardStyle.STYLE_1
                }
                AdCardPreview(course, AdCardStyle.STYLE_2, selectedStyle == AdCardStyle.STYLE_2) {
                    selectedStyle = AdCardStyle.STYLE_2
                }
                AdCardPreview(course, AdCardStyle.STYLE_3, selectedStyle == AdCardStyle.STYLE_3) {
                    selectedStyle = AdCardStyle.STYLE_3
                }

                Button(
                    onClick = { onStyleSelected(selectedStyle) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
                ) {
                    Text("Select Design", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdCardPreview(
    course: Courses,
    style: AdCardStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val actualBorderColor = if (isSelected) HoloCyan else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(borderWidth, actualBorderColor, RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoostedCourseCard(course = course, style = style)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when(style) {
                AdCardStyle.STYLE_1 -> "Minimal Clean"
                AdCardStyle.STYLE_2 -> "Modern Overlay"
                AdCardStyle.STYLE_3 -> "Vibrant Gradient"
            },
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) HoloCyan else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BoostedCourseCard(
    course: Courses,
    style: AdCardStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when (style) {
            AdCardStyle.STYLE_1 -> MinimalStyle(course)
            AdCardStyle.STYLE_2 -> ModernStyle(course)
            AdCardStyle.STYLE_3 -> VibrantStyle(course)
        }
    }
}

@Composable
fun MinimalStyle(course: Courses) {
    Row(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AsyncImage(
            model = course.imageUrl,
            contentDescription = null,
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.weight(0.6f).padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(course.name, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(course.instructorName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Learn Now",
                color = HoloPurple,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun ModernStyle(course: Courses) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = course.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Surface(
                color = HoloCyan,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    "PREMIUM",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(course.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(course.instructorName, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun VibrantStyle(course: Courses) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(HoloPurple, HoloCyan)
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                Text(
                    course.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontFamily = IbarraNovaFont
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "By ${course.instructorName}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AsyncImage(
                model = course.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SubscriptionPlanDialog(
    onDismiss: () -> Unit,
    onPlanSelected: (String, Int) -> Unit // duration, price
) {
    val plans = listOf(
        Triple("7 Days", 3, "Essential reach"),
        Triple("1 Month", 9, "Most Popular"),
        Triple("3 Months", 21, "Best Value")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Boost Plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                plans.forEach { (duration, price, tag) ->
                    PlanItem(duration, price, tag) {
                        onPlanSelected(duration, price)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PlanItem(duration: String, price: Int, tag: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(duration, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(tag, style = MaterialTheme.typography.bodySmall, color = HoloCyan)
            }
            Text("$$price", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = HoloPurple)
        }
    }
}

@Composable
fun PaymentSimulationDialog(
    onSuccess: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        isProcessing = false
        kotlinx.coroutines.delay(1500)
        onSuccess()
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = HoloPurple, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Processing Payment...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Successful!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Your course is now boosted!", textAlign = TextAlign.Center)
                }
            }
        }
    }
}
