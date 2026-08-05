package com.example.holoverse.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.chat.domain.model.Message
import com.example.holoverse.chat.domain.model.MessageStatus

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {},
    onGlbClick: ((String, String) -> Unit)? = null,
    onVoteClick: (Int) -> Unit = {},
    onFinalizeBooking: (Long) -> Unit = {},
    senderImageUrl: String? = null,
    showSenderInfo: Boolean = false,
    currentUserId: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 12.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isCurrentUser && showSenderInfo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (senderImageUrl != null) {
                                AsyncImage(
                                    model = senderImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = message.senderName.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (message.imageUrl != null) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = stringResource(R.string.image_message),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.poll != null) {
                    PollContent(
                        poll = message.poll,
                        isCurrentUser = isCurrentUser,
                        currentUserId = currentUserId,
                        onVoteClick = onVoteClick
                    )
                }

                if (message.bookingRequest != null) {
                    BookingCard(
                        bookingRequest = message.bookingRequest,
                        isCurrentUser = isCurrentUser,
                        isMentor = showSenderInfo, // Simplified check, ideally pass explicitly
                        onFinalizeClick = onFinalizeBooking,
                        onVoteClick = onVoteClick
                    )
                }

                if (message.videoUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = stringResource(R.string.video_message),
                                tint = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = stringResource(R.string.video_message),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.fileUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentUser) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(R.string.file),
                            tint = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.fileName ?: stringResource(R.string.document),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.glbUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentUser) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = onGlbClick != null) {
                                onGlbClick?.invoke(
                                    message.glbUrl,
                                    message.fileName ?: "3D Model"
                                )
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ViewInAr,
                            contentDescription = stringResource(R.string.glb_model),
                            tint = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.fileName ?: stringResource(R.string.glb_model),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (message.audioUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = if (message.text.isNotBlank()) 4.dp else 0.dp)
                            .clickable { onPlayClick() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) stringResource(R.string.pause_audio) else stringResource(
                                R.string.play_audio
                            ),
                            tint = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) stringResource(R.string.playing) else stringResource(
                                R.string.voice_message
                            ),
                            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (isCurrentUser) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (message.status) {
                            MessageStatus.SENDING -> {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = stringResource(R.string.sending),
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            MessageStatus.FAILED -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = stringResource(R.string.failed),
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Red
                                )
                            }

                            MessageStatus.SENT -> {
                                // Optional: Show a checkmark or nothing
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PollContent(
    poll: com.example.holoverse.chat.domain.model.Poll,
    isCurrentUser: Boolean,
    currentUserId: String,
    onVoteClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = poll.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
        )

        val totalVotes = poll.votes.values.sumOf { it.size }.coerceAtLeast(1)

        poll.options.forEachIndexed { index, option ->
            val optionVotes = poll.votes[index.toString()]?.size ?: 0
            val percentage = (optionVotes.toFloat() / totalVotes.toFloat())
            val hasVoted = poll.votes[index.toString()]?.contains(currentUserId) == true

            Surface(
                onClick = { onVoteClick(index) },
                shape = RoundedCornerShape(12.dp),
                color = if (hasVoted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasVoted) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Progress background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .height(44.dp)
                            .background(
                                (if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary)
                                    .copy(alpha = 0.1f)
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (hasVoted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (hasVoted) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "$optionVotes",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                }
            }
        }
        
        Text(
            text = "${poll.votes.values.sumOf { it.size }} votes",
            style = MaterialTheme.typography.labelSmall,
            color = if (isCurrentUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun SendingVoiceBubble() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 0.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.sending_voice_message),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
