package com.example.holoverse.ui.transaction

import androidx.compose.foundation.background
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
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.ui.theme.primaryLight
import com.example.holoverse.ui.theme.secondaryLight

data class TransactionItem(
    val title: String,
    val category: String,
    val status: String,
)
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val transactions = listOf(
        TransactionItem("Build Personal Branding", "Web Designer", "Paid"),
        TransactionItem("Mastering Blender 3D", "Ui/UX Designer", "Received"),
        TransactionItem("Full Stack Web Developer", "Web Development", "Paid"),
        TransactionItem("Complete UI Designer", "HR Management", "Received"),
        TransactionItem("Sharing Work with Team", "Finance & Accounting", "Paid")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = IbarraNovaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionCard(transaction)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Image Placeholder
        Box(
            modifier = Modifier
                .size(85.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
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
                    color = Color(0xFF1B1B21)
                )
            )
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = IbarraNovaFont,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            val badgeColor = if (transaction.status == "Paid") {
                secondaryLight // Teal
            } else {
                primaryLight // Received on primary color
            }

            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = transaction.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
