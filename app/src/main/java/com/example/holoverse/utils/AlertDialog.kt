package com.example.holoverse.utils

import android.provider.CalendarContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.holoverse.R
import com.example.holoverse.ui.spatialTheme.SpatialBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogSample(
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit


) {

    val openDialog = remember {
        mutableStateOf(true)
    }

    if (openDialog.value) {


        AlertDialog(
            onDismissRequest = onDismissClick,
            title = {
                Text(text = title)
            },
            text = {
                Text(
                    text = text
                )
            },

            confirmButton = {
                TextButton(onClick = onConfirmClick) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissClick

                ) { Text("Dismiss") }
            },
            icon = {
                Image(
                    painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null
                )
            },
            tonalElevation = 5.dp,


            )

    }


}


@Composable
fun AnimatedAlertDialog(
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        Dialog(onDismissRequest = onDismissClick) {
            // Use a Surface for elevation, shape, and clipping the background
            Surface(
                shape = RoundedCornerShape(28.dp), // Standard shape for Material3 dialogs
                tonalElevation = 5.dp,
                modifier = Modifier.height(400.dp)
            ) {
                // Box to layer the animated background and the content
                Box {
                    // Your animated background
                    SpatialBackground()

                    // Column for the dialog content (icon, title, text, buttons)
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                            )
                        }

                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Text
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismissClick) {
                                Text("Dismiss")
                            }
                            TextButton(onClick = onConfirmClick) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlertDialogPreview() {
    AnimatedAlertDialog(
        title = "SignIn",
        text = " You Cant Write an Review Before Sign up Please Sign up And Try Again ",
        onConfirmClick = {},
        onDismissClick = {}
    )

}