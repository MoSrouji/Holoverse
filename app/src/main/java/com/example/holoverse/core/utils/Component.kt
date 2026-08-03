package com.example.holoverse.core.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.BorderWhite
import com.example.holoverse.core.ui.theme.GlassWhite
import com.example.holoverse.navigation.AppDestination
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.navigation.NavigationState

@Composable
fun HoloBottomDock(
    navigationState: NavigationState,
    navigator: AppNavigator,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush(darkTheme))
            .height(58.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.padding(5.dp))

        val isCategorySelected = navigationState.topLevelRoute == AppDestination.Category
        NavBarItem(
            icon = Icons.AutoMirrored.Default.ViewList,
            isSelected = isCategorySelected,
            darkTheme = darkTheme,
            contentDescription = "Categories"
        ) {
            navigator.navigateTo(AppDestination.Category)
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isChatSelected = navigationState.topLevelRoute == AppDestination.ChatList
        NavBarItem(
            icon = Icons.AutoMirrored.Filled.Message,
            isSelected = isChatSelected,
            darkTheme = darkTheme,
            contentDescription = "Messages"
        ) {
            navigator.navigateTo(AppDestination.ChatList)
        }


        Spacer(modifier = Modifier.padding(5.dp))

        val isHomeSelected = navigationState.topLevelRoute == AppDestination.HomeScreen
        NavBarItem(
            icon = Icons.Default.Home,
            isSelected = isHomeSelected,
            darkTheme = darkTheme,
            contentDescription = "Home",
            modifier = Modifier
                .padding(bottom = 5.dp)
                .size(40.dp)
        ) {
            navigator.navigateTo(AppDestination.HomeScreen)
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isGallerySelected = navigationState.topLevelRoute == AppDestination.GalleryScreen
        NavBarItem(
            icon = Icons.Default.Storefront,
            isSelected = isGallerySelected,
            darkTheme = darkTheme,
            contentDescription = "Gallery"
        ) {
            navigator.navigateTo(AppDestination.GalleryScreen)
        }

        Spacer(modifier = Modifier.padding(5.dp))

        val isProfileSelected = navigationState.topLevelRoute == AppDestination.Profile
        NavBarItem(
            icon = Icons.Default.PersonOutline,
            isSelected = isProfileSelected,
            darkTheme = darkTheme,
            contentDescription = "Profile"
        ) {
            navigator.navigateTo(AppDestination.Profile)
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
    contentDescription: String? = null,
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
            contentDescription = contentDescription,
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
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
            .border(1.dp, BorderWhite, RoundedCornerShape(cornerRadius))
            .clickable(
                onClick = onClick,
                enabled = enable
            ),
        content = content,
    )
}

