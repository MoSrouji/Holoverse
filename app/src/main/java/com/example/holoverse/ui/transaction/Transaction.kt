package com.example.holoverse.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.BorderWhite
import com.example.holoverse.ui.theme.GlassWhite
import com.example.holoverse.ui.theme.HoloCyan
import com.example.holoverse.ui.theme.HoloPurple
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.ui.theme.ColorPlatinum
import com.example.holoverse.ui.theme.HoloverseTheme

data class TransactionItem(
    val title: String,
    val category: String,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    appNavigator: AppNavigator
) {
    val transactions = listOf(
        TransactionItem("Build Personal Branding", "Web Designer", "Paid"),
        TransactionItem("Mastering Blender 3D", "Ui/UX Designer", "Received"),
        TransactionItem("Full Stack Web Developer", "Web Development", "Paid"),
        TransactionItem("Complete UI Designer", "HR Management", "Received"),
        TransactionItem("Sharing Work with Team", "Finance & Accounting", "Paid")
    )

    Box(modifier = Modifier.fillMaxSize()) {
      //  SpatialBackground()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = IbarraNovaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { appNavigator.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, BorderWhite, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Image Placeholder
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, BorderWhite, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = IbarraNovaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            )
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = IbarraNovaFont,
                    color = ColorPlatinum.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            val (badgeColor, textColor) = if (transaction.status == "Paid") {
                HoloPurple to Color.White
            } else {
                HoloCyan to Color.Black
            }

            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = transaction.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    HoloverseTheme(darkTheme = true ) {
        TransactionScreen(appNavigator = AppNavigator())

    }
}

