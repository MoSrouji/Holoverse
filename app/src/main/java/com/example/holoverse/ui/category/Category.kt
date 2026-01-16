package com.example.holoverse.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Category(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen() {
    val categories = remember {
        listOf(
            Category("3D Design", Icons.Default.Category),
            Category("Graphic Design", Icons.Default.Draw),
            Category("Web Development", Icons.Default.Code),
            Category("SEO & Marketing", Icons.Default.TrendingUp),
            Category("Finance & Accounting", Icons.Default.AccountBalance),
            Category("Personal Development", Icons.Default.SelfImprovement),
            Category("Office Productivity", Icons.Default.Work),
            Category("HR Management", Icons.Default.Groups)
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF7F8FC)
                )
            )
        },
        containerColor = Color(0xFFF7F8FC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for..", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .background(Color(0xFF0D62D9), RoundedCornerShape(8.dp))
                                .size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(category)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.name,
            modifier = Modifier.size(40.dp),
            tint = Color.Black.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen()
}
