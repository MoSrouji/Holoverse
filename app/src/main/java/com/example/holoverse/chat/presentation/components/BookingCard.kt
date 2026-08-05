package com.example.holoverse.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.holoverse.chat.domain.model.BookingRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookingCard(
    bookingRequest: BookingRequest,
    isCurrentUser: Boolean,
    isMentor: Boolean,
    onFinalizeClick: (Long) -> Unit = {},
    onVoteClick: (Int) -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Event,
                contentDescription = null,
                tint = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Proposed Lesson Times",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        bookingRequest.proposedTimes.forEachIndexed { index, timeMillis ->
            val dateStr = dateFormat.format(Date(timeMillis * 1000))
            
            Surface(
                onClick = { if (!isMentor) onVoteClick(index) },
                shape = RoundedCornerShape(12.dp),
                color = if (isCurrentUser) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                border = if (bookingRequest.status == "CONFIRMED") {
                    androidx.compose.foundation.BorderStroke(2.dp, Color.Green)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    if (isMentor && bookingRequest.status == "PENDING") {
                        Button(
                            onClick = { onFinalizeClick(timeMillis) },
                            modifier = Modifier.size(height = 32.dp, width = 80.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                                contentColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.White
                            )
                        ) {
                            Text("Select", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (bookingRequest.status == "CONFIRMED") {
                         Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Confirmed",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (bookingRequest.status == "CONFIRMED") {
            Text(
                text = "Session Confirmed!",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
