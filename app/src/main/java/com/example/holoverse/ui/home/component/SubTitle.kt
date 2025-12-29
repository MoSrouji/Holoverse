package com.example.holoverse.ui.home.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.R

@Composable
fun SubTitle(
   @StringRes text: Int,
//    onSubTitleButtonClick:()-> Unit
){
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(8.dp) ,
        horizontalArrangement = Arrangement.SpaceBetween ,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text =stringResource(text),
            style = MaterialTheme.typography.titleLarge ,
            fontWeight = FontWeight.Bold,
        )
        Row(modifier = Modifier ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(text = "SEE ALL " ,
                color = MaterialTheme.colorScheme.tertiary ,
                fontWeight = FontWeight.Bold ,
                style = MaterialTheme.typography.bodyMedium)

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "More discover movies",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary

            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubTitlePreview(){


}