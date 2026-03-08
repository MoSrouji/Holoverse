package com.example.holoverse.ui.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.utils.GlassCard

@Composable
fun TeacherCard(mentor: User.Mentor, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        GlassCard(
            modifier = Modifier
                .height(80.dp)
                .width(80.dp),
            cornerRadius = 20.dp,
            onClick = {},
            enable = true,
        ) {
            AsyncImage(
                model = null, // Add mentor image URL here
                contentDescription = "Mentor Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612)
            )
        }
        Spacer(modifier = Modifier.padding(bottom = 5.dp))
        Text(
            text = mentor.fullName ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
fun TeacherCardPreview() {
    TeacherCard(
        mentor = User.Mentor(
            fullName = "Mohammad"
        )
    )
}
