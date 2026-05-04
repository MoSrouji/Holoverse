package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.R
import com.example.holoverse.ui.theme.IbarraNovaFont
import java.util.Calendar

@Composable
fun HomeScreenHeader(
    fullName: String?,
    darkTheme: Boolean,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    brush: (Boolean) -> Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(brush(darkTheme))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(getGreeting()),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = fullName ?: stringResource(R.string.guest),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            fontFamily = IbarraNovaFont
                        ),
                    )
                }

                IconButton(
                    onClick = onNavigateToNotifications
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = stringResource(R.string.notifications_desc),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeSearchBar(
                onSearchClick = onNavigateToSearch
            )

            Spacer(modifier = Modifier.height(24.dp))

            CarouselAdds()

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun getGreeting(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> R.string.good_morning
        in 12..16 -> R.string.good_afternoon
        else -> R.string.good_evening
    }
}
