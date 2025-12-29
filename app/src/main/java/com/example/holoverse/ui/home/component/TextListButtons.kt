package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview(showBackground = true)
fun TextListButton() {
   val itemList = listOf<String>("3D Design ", "Arts & Humanities",
        "Graphic Design", "Programming" , " Accounting " , " Math ")


    LazyRow(
        modifier = Modifier.padding(5.dp) ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        items(itemList){
            Spacer(modifier = Modifier.padding(start = 5.dp))

            Text(text = it ,
                style = MaterialTheme.typography.bodyLarge ,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                fontWeight = FontWeight.W600
            )

        }
    }



}

@Composable
@Preview
fun TextListTextButton() {

   val itemList = listOf<String>("3D Design ", "Arts & Humanities",
        "Graphic Design", "Programming" , " Accounting " , " Math ")


    LazyRow(
        modifier = Modifier.padding(5.dp) ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        items(itemList) {
            Spacer(modifier = Modifier.padding(start = 5.dp))

            TextButton(
                onClick = {} ,
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondary ,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                   color = MaterialTheme.colorScheme.surfaceDim,
                    fontWeight = FontWeight.W600
                )

            }
        }
    }



}
