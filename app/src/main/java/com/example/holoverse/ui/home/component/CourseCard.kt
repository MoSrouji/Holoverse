package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview(showBackground = true)
fun CourseCard(
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .height(240.dp)
            .width(
                280.dp
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column() {
            Column(
                modifier = Modifier
                    .fillMaxHeight(.50f)
                    .background(
                        color = MaterialTheme.colorScheme.background
                    )
            ) {


            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                CourseTypeWithButton()
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Graphic Design Advanced",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(8.dp))

                CourseBottomDivider()


            }

        }


    }


}

@Composable
@Preview(showBackground = true)
fun CourseTypeWithButton(

) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Graphic Design",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.BookmarkAdd, contentDescription = "Save For Later "
        )


    }
}


@Composable
@Preview(showBackground = true)
fun CourseBottomDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(.80f),
        horizontalArrangement = Arrangement.Absolute.SpaceAround
    ) {
        Text(
            text = " $28"
        )
        VerticalDivider(
            modifier = Modifier

                .height(20.dp),
            thickness = 3.dp

            )
        Text(
            text = "4.2"
        )
        VerticalDivider(
            modifier = Modifier.height(
                20.dp
            ),
            thickness = 3.dp

        )
        Text(
            text = "7830 Std "
        )
    }


}
