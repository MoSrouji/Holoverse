import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.reviews.ui.components.LiveMentorRating
import com.example.holoverse.utils.GlassCard

@Composable
fun TeacherCard(
    mentor: User.Mentor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Column(
        modifier = modifier.width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassCard(
            modifier = Modifier
                .size(100.dp),
            cornerRadius = 24.dp,
            onClick = onClick,
            enable = true,
        ) {
            AsyncImage(
                model = mentor.profileImageUrl,
                contentDescription = "Mentor Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                error = painterResource(R.drawable.istockphoto_1934800957_612x612)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mentor.fullName ?: "Unknown",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = stringResource(mentor.specialization.titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        LiveMentorRating(
            mentorId = mentor.userId ?: "",
            initialRating = mentor.averageRating ?: 0.0,
            initialReviewsCount = mentor.reviewsCount ?: 0,
            textStyle = MaterialTheme.typography.labelMedium,
            iconSize = 14.dp,
            modifier = Modifier.padding(top = 4.dp)
        )

        mentor.hourlyRate?.let { rate ->
            Text(
                text = stringResource(R.string.price_format, rate) + "/hr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TeacherCardPreview() {
    TeacherCard(
        mentor = User.Mentor(
            fullName = "Mohammad",
            averageRating = 4.8
        )
    )
}
