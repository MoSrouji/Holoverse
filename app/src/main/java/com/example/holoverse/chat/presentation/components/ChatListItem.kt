package com.example.holoverse.chat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User

@Composable
fun ChatListItem(name: String, lastMessage: String, imageUrl: String? = null, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(lastMessage, maxLines = 1) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                            error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                        )
                    } else {
                        Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    )
}

@Composable
fun ContactListItem(mentor: User.Mentor, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                mentor.fullName ?: stringResource(R.string.unknown),
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = { Text(stringResource(R.string.mentor_label)) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (mentor.profileImageUrl != null) {
                        AsyncImage(
                            model = mentor.profileImageUrl,
                            contentDescription = mentor.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                            error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                        )
                    } else {
                        Text(
                            (mentor.fullName ?: stringResource(R.string.unknown)).take(1)
                                .uppercase()
                        )
                    }
                }
            }
        }
    )
}

