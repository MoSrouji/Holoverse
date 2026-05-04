package com.example.holoverse.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.R
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.ui.theme.HoloverseTheme


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.platform.LocalContext
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.ui.spatialTheme.Brush

data class Category(val key: String, val nameRes: Int, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBackClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    darkTheme: Boolean
) {
    val context = LocalContext.current
    val categories = remember {
        listOf(
            Category("3D DESIGN", R.string.category_3d_design, Icons.Default.Category),
            Category("GRAPHIC DESIGN", R.string.category_graphic_design, Icons.Default.Draw),
            Category("WEB DEVELOPMENT", R.string.category_web_development, Icons.Default.Code),
            Category("SEO MARKETING", R.string.category_seo_marketing,
                Icons.AutoMirrored.Filled.TrendingUp
            ),
            Category("FINANCE & ACCOUNTING", R.string.category_finance_accounting, Icons.Default.AccountBalance),
            Category("PERSONAL DEVELOPMENT", R.string.category_personal_development, Icons.Default.SelfImprovement),
            Category("OFFICE PRODUCTIVITY", R.string.category_office_productivity, Icons.Default.Work),
            Category("HR MANAGEMENT", R.string.category_hr_management, Icons.Default.Groups),
            Category("DATA SCIENCE", R.string.category_data_science, Icons.Default.Science),
            Category("MOBILE DEVELOPMENT", R.string.category_mobile_development, Icons.Default.Smartphone),
            Category("MUSIC", R.string.category_music, Icons.Default.MusicNote),
            Category("PHOTOGRAPHY", R.string.category_photography, Icons.Default.CameraAlt),
            Category("BUSINESS", R.string.category_business, Icons.Default.BusinessCenter),
            Category("HEALTH & FITNESS", R.string.category_health_fitness, Icons.Default.FitnessCenter),
            Category("LANGUAGE LEARNING", R.string.category_language_learning, Icons.Default.Language),
            Category("AI", R.string.category_ai, Icons.Default.AutoAwesome),
            Category("MATHEMATICS", R.string.category_mathematics, Icons.Default.Calculate),
            Category("SCIENCE", R.string.category_science, Icons.Default.Science),
            Category("LANGUAGES", R.string.category_languages, Icons.Default.Language),
            Category("HUMANITIES", R.string.category_humanities, Icons.AutoMirrored.Filled.MenuBook),
            Category("TEST PREP", R.string.category_test_prep, Icons.AutoMirrored.Filled.Assignment),
            Category("COMPUTER SCIENCE", R.string.category_computer_science, Icons.Default.Code),
            Category("ARTS", R.string.category_arts, Icons.Default.Brush),
            Category("SPECIAL EDUCATION", R.string.category_special_education, Icons.Default.Psychology),
            Category("PHYSICS", R.string.category_physics, Icons.Default.Biotech),
            Category("CHEMISTRY", R.string.category_chemistry, Icons.Default.Science),
            Category("BIOLOGY", R.string.category_biology, Icons.Default.Science),
            Category("HISTORY", R.string.category_history, Icons.Default.HistoryEdu),
            Category("PHILOSOPHY", R.string.category_philosophy, Icons.Default.Lightbulb),
            Category("PSYCHOLOGY", R.string.category_psychology, Icons.Default.Psychology),
            Category("ECONOMICS", R.string.category_economics, Icons.AutoMirrored.Filled.TrendingUp),
            Category("ENTREPRENEURSHIP", R.string.category_entrepreneurship, Icons.Default.Business),
            Category("ELEMENTARY EDUCATION", R.string.category_elementary_education, Icons.Default.ChildCare),
            Category("STUDY SKILLS", R.string.category_study_skills, Icons.Default.Edit)
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))

            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                        
                        Text(
                            text = stringResource(R.string.all_categories),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = IbarraNovaFont
                            ),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Search Bar inside Header
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                text = stringResource(R.string.search_for), 
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_icon),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        singleLine = true
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(category) {
                            onCategorySelected(category.key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(category.nameRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        CategoryScreen(
            onBackClick = {},
            onCategorySelected = {},
            darkTheme = true
        )
    }
}
