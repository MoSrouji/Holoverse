package com.example.holoverse.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.ui.theme.HoloverseTheme

data class ProfileItemData(val icon: ImageVector, val title: String, val endText: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {

    val profileItems = listOf(
        ProfileItemData(Icons.Default.Person, "Edit Profile"),
        ProfileItemData(Icons.Default.Payment, "Payment Option"),
        ProfileItemData(Icons.Default.Notifications, "Notifications"),
        ProfileItemData(Icons.Default.GppGood, "Security"),
        ProfileItemData(Icons.Default.Language, "Language", "English (US)"),
        ProfileItemData(Icons.Default.DarkMode, "Dark Mode"),
        ProfileItemData(Icons.Default.Policy, "Terms & Conditions"),
        ProfileItemData(Icons.AutoMirrored.Filled.HelpOutline, "Help Center"),
        ProfileItemData(Icons.AutoMirrored.Filled.Message, "Invite Friends"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(4.dp, Color(0xFF009688), CircleShape)

                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-4).dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Edit Image", tint = Color(0xFF009688))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("James S. Hernandez", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("hernandex.redial@gmail.ac.in", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    profileItems.forEachIndexed { index, item ->
                        ProfileItem(item = item)
                        if (index < profileItems.lastIndex) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(item: ProfileItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = item.title, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(item.title, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        item.endText?.let {
            Text(it, color = Color(0xFF007AFF), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    HoloverseTheme {
        ProfileScreen()
    }
}
