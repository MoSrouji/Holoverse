package com.example.holoverse.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.holoverse.core.ui.spatial.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit,
    darkTheme: Boolean = true
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush(darkTheme))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Poll",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Options",
                    style = MaterialTheme.typography.labelLarge,
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(options) { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newVal ->
                                    val newList = options.toMutableList()
                                    newList[index] = newVal
                                    options = newList
                                },
                                label = { Text("Option ${index + 1}") },
                                modifier = Modifier.weight(1f),
                            )
                            if (options.size > 2) {
                                IconButton(onClick = {
                                    options = options.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }

                if (options.size < 6) {
                    TextButton(
                        onClick = { options = options + "" },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Option")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val validOptions = options.filter { it.isNotBlank() }
                            if (question.isNotBlank() && validOptions.size >= 2) {
                                onCreate(question, validOptions)
                            }
                        },
                        enabled = question.isNotBlank() && options.count { it.isNotBlank() } >= 2,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
