package com.example.holoverse.user.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onBackClick: () -> Unit, darkTheme: Boolean = true) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.terms_conditions),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Terms and Conditions for Holoverse",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TermSection(
                title = "1. Introduction",
                content = "Welcome to Holoverse, an Augmented Reality (AR) based learning platform. By using our application, you agree to these terms. Our service facilitates learning through real-time video calls enhanced with AR technology."
            )

            TermSection(
                title = "2. AR Safety & Environment",
                content = "When using AR features during video calls, ensure you are in a safe, clear physical environment. Holoverse is not responsible for any physical injury or property damage resulting from use of the AR interface. Always remain aware of your real-world surroundings."
            )

            TermSection(
                title = "3. Video Call & Camera Usage",
                content = "To facilitate learning, Holoverse requires access to your camera and microphone. You agree to use these features solely for educational purposes. Any inappropriate behavior, harassment, or unauthorized recording of video calls is strictly prohibited and may lead to account termination."
            )

            TermSection(
                title = "4. User Conduct",
                content = "Users must maintain a professional and respectful demeanor during interactions. Mentors and students are expected to follow the curriculum and respect privacy boundaries. Do not share personal contact information outside the platform."
            )

            TermSection(
                title = "5. Privacy and Data",
                content = "We value your privacy. Camera feeds are processed in real-time to overlay AR elements. Please refer to our Privacy Policy for details on how we handle your data and media streams."
            )

            TermSection(
                title = "6. Modifications to Service",
                content = "Holoverse reserves the right to modify or discontinue any part of the AR learning experience at any time. We will notify users of significant changes to these terms."
            )

            Text(
                text = "Last updated: October 2026",
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
fun TermSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

