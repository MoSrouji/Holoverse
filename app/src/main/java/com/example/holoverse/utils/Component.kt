package com.example.holoverse.utils

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
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOutMap
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.ui.spatialTheme.Brush
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.BorderWhite
import com.example.holoverse.ui.theme.GlassWhite

@Composable
fun HoloBottomDock(
    navController: NavController = rememberNavController(),
    darkTheme: Boolean
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush(darkTheme))
            .height(58.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.padding(5.dp))

        val isCategorySelected =
            currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.Category>() } == true
        NavBarItem(
            icon = Icons.AutoMirrored.Default.ViewList,
            isSelected = isCategorySelected,
            darkTheme = darkTheme
        ) {
            if (!isCategorySelected) {
                navController.navigate(AppDestination.Category) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isChatSelected =
            currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.ChatList>() } == true
        NavBarItem(
            icon = Icons.AutoMirrored.Filled.Message,
            isSelected = isChatSelected,
            darkTheme = darkTheme
        ) {
            if (!isChatSelected) {
                navController.navigate(AppDestination.ChatList) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }


        Spacer(modifier = Modifier.padding(5.dp))

        val isHomeSelected =
            currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.HomeScreen>() } == true
        NavBarItem(
            icon = Icons.Default.Home,
            isSelected = isHomeSelected,
            darkTheme = darkTheme,
            modifier = Modifier
                .padding(bottom = 5.dp)
                .size(40.dp)
        ) {
            if (!isHomeSelected) {
                navController.navigate(AppDestination.HomeScreen) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isGallerySelected = currentDestination?.hierarchy?.any {
            it.hasRoute<AppDestination.GalleryScreen>() || it.hasRoute<AppDestination.ModelGraph>()
        } == true
        NavBarItem(
            icon = Icons.Default.Storefront,
            isSelected = isGallerySelected,
            darkTheme = darkTheme
        ) {
            if (!isGallerySelected) {
                navController.navigate(AppDestination.ModelGraph) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isProfileSelected =
            currentDestination?.hierarchy?.any { it.hasRoute<AppDestination.Profile>() } == true
        NavBarItem(
            icon = Icons.Default.PersonOutline,
            isSelected = isProfileSelected,
            darkTheme = darkTheme
        ) {
            if (!isProfileSelected) {
                navController.navigate(AppDestination.Profile) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))
    }


}

@Composable
fun NavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    isSelected: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isSelected) {
        MaterialTheme.colorScheme.secondary
    } else {
        if (darkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
    }

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