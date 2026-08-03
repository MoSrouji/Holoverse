package com.example.holoverse.home.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.core.ui.theme.ColorBlue
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.IbarraNovaFont
import com.example.holoverse.core.utils.GlassCard
import java.util.Calendar

@Composable
fun UserIdentityCard(
    modifier: Modifier = Modifier,
    fullName: String?,
    profileImageUrl: String?,
    onClick: () -> Unit = {},
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        onClick = onClick,
        enable = true,
        color = ColorBlue.copy(alpha = 0.0f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = stringResource(R.string.profile),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, HoloCyan.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.account_circle_24px),
                error = painterResource(R.drawable.account_circle_24px)
            )

            Column {
                Text(
                    text = stringResource(getGreeting()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = fullName ?: stringResource(R.string.guest),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontFamily = IbarraNovaFont
                    ),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun getGreeting(): Int {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> R.string.good_morning
        in 12..16 -> R.string.good_afternoon
        else -> R.string.good_evening
    }
}
