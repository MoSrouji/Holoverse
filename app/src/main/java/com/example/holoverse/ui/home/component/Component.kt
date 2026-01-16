package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.BorderWhite
import com.example.holoverse.ui.theme.GlassWhite

@Preview
@Composable
fun HoloBottomDock(navController: NavController = rememberNavController()) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        GlassCard(
            modifier = Modifier
                .height(58.dp),
            cornerRadius = 0.dp,
            color = Color(0xFF1A1A24),
            onClick = {},
            enable = false
        ) {
            SpatialBackground()
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.padding(5.dp))

                NavBarItem(
                    icon = Icons.AutoMirrored.Default.ViewList,
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.Category>() } == true
                ) { navController.navigate(AppDestination.Category) }
                
                Spacer(modifier = Modifier.padding(5.dp))

                NavBarItem(
                    icon = Icons.AutoMirrored.Filled.Message,
                    isSelected = false // Handle route if needed
                ) { /* TODO */ }
                
                Spacer(modifier = Modifier.padding(5.dp))
                
                NavBarItem(
                    icon = Icons.Default.Home,
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.HomeScreen>() } == true,
                    modifier = Modifier.padding(bottom = 5.dp).size(40.dp)
                ) { navController.navigate(AppDestination.HomeScreen) }
                
                Spacer(modifier = Modifier.padding(5.dp))
                
                NavBarItem(
                    icon = Icons.Default.Payment,
                    isSelected = false // Handle route if needed
                ) { /* TODO */ }
                
                Spacer(modifier = Modifier.padding(5.dp))
                
                NavBarItem(
                    icon = Icons.Default.PersonOutline,
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.Profile>() } == true
                ) { navController.navigate(AppDestination.Profile) }
                
                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }
}

@Composable
fun NavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.4f)

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = modifier.size(30.dp)
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    color: Color = GlassWhite,
    onClick: () -> Unit,
    enable: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enable
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
            .border(1.dp, BorderWhite, RoundedCornerShape(cornerRadius)),
        content = content,
    )
}
