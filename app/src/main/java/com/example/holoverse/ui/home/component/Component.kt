package com.example.holoverse.ui.home.component

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.automirrored.filled.MultilineChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.holoverse.R
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.BorderWhite
import com.example.holoverse.ui.theme.GlassWhite

@Preview
@Composable
fun HoloBottomDock(navController: NavController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth(),
           // .padding(bottom = 5.dp),
        contentAlignment = Alignment.Center
    ) {

        GlassCard(
            modifier = Modifier.padding(5.dp)
                .height(60.dp),
            cornerRadius = 20.dp,
            color = Color(0xFF1A1A24),
            onClick = {} ,
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
                    Icons.Default.Home,
                    "home",
                    currentRoute
                ) { navController.navigate("home") }
               Spacer(modifier = Modifier.padding(5.dp))
                NavBarItem(
                    Icons.AutoMirrored.Default.ViewList
                ,
                    "mentors",
                    currentRoute
                ) { navController.navigate("mentors") }
              Spacer(modifier = Modifier.padding(5.dp))

                NavBarItem(
                    Icons.AutoMirrored.Filled.Message ,
                    "ar_session",
                    currentRoute
                ) { navController.navigate("ar_session") }
                Spacer(modifier = Modifier.padding(5.dp))
                NavBarItem(
                  icon = Icons.Default.Payment,
                    "ar_session",
                    currentRoute
                ) { navController.navigate("ar_session") }
                Spacer(modifier = Modifier.padding(5.dp))
                NavBarItem(
                    Icons.Default.PersonOutline,
                    "ar_session",
                    currentRoute
                ) { navController.navigate("ar_session") }
                Spacer(modifier = Modifier.padding(5.dp))



            }
        }
    }
}

@Composable
fun NavBarItem(icon: ImageVector,
               route: String, currentRoute: String?,
               onClick: () -> Unit) {
    val selected = currentRoute == route
    val color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f)
    val scale by animateFloatAsState(if (selected) 1.2f else 1f, label = "scale")

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(36.dp).scale(scale)
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    color: Color = GlassWhite,
    onClick: () -> Unit ,
    enable : Boolean,
    content: @Composable BoxScope.() -> Unit,

    ) {
    Box(
        modifier = modifier.clickable(
            onClick = {onClick} ,
            enabled = enable
        )
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
            .border(1.dp, BorderWhite, RoundedCornerShape(cornerRadius)),
        content = content,

        )
        //SpatialBackground()

}
