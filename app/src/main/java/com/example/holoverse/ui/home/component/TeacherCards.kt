package com.example.holoverse.ui.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.R

@Composable
@Preview
fun TeacherCard( modifier: Modifier = Modifier) {

    Column(modifier = modifier) {
        GlassCard(
            modifier = Modifier
                .height(70.dp)
                .width(80.dp),
            cornerRadius = 20.dp,
            onClick = {},
            enable = true,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painterResource(
                        R.drawable.istockphoto_1934800957_612x612 ,

                    ) ,
                    contentDescription = "Teacher " ,
                    contentScale = ContentScale.Crop

                    )


            }


        }
        Spacer(modifier = Modifier.padding(bottom = 5.dp))
        Text(
            text = " Mohammad "
        )
    }
}

