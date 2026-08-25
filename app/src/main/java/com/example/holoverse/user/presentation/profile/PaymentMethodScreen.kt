package com.example.holoverse.user.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    onBackClick: () -> Unit,
    darkTheme: Boolean,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val backgroundBrush = remember(darkTheme) { Brush(darkTheme) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.payment_option),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = IbarraNovaFont,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Real Balance Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, BorderWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Total Balance",
                                color = if (darkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$${String.format("%.2f", uiState.balance)}",
                                color = if (darkTheme) Color.White else Color.Black,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = IbarraNovaFont
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.addFunds(500.0) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(HoloCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = HoloCyan)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Add Funds ($500)",
                                    color = if (darkTheme) Color.White else Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Add New Card Section
                item {
                    Text(
                        text = "Add New Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, BorderWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PaymentTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it },
                                label = "Card Holder Name",
                                placeholder = "John Doe",
                                darkTheme = darkTheme
                            )
                            PaymentTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) cardNumber = it },
                                label = "Card Number",
                                placeholder = "0000 0000 0000 0000",
                                keyboardType = KeyboardType.Number,
                                darkTheme = darkTheme
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                PaymentTextField(
                                    modifier = Modifier.weight(1f),
                                    value = expiryDate,
                                    onValueChange = { if (it.length <= 5) expiryDate = it },
                                    label = "Expiry Date",
                                    placeholder = "MM/YY",
                                    keyboardType = KeyboardType.Number,
                                    darkTheme = darkTheme
                                )
                                PaymentTextField(
                                    modifier = Modifier.weight(1f),
                                    value = cvv,
                                    onValueChange = { if (it.length <= 3) cvv = it },
                                    label = "CVV",
                                    placeholder = "123",
                                    keyboardType = KeyboardType.Number,
                                    darkTheme = darkTheme
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    isSaving = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HoloPurple,
                                    contentColor = Color.White
                                ),
                                enabled = !isSaving && cardHolder.isNotEmpty() && cardNumber.length >= 16
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save Payment Method", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Existing Methods
                item {
                    Text(
                        text = "Saved Methods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) Color.White else Color.Black
                    )
                }

                items(listOf("Visa", "MasterCard")) { type ->
                    SavedMethodItem(type = type, darkTheme = darkTheme)
                }
            }

            // Success Dialog Simulation
            if (isSaving) {
                LaunchedEffect(Unit) {
                    delay(2000.milliseconds)
                    isSaving = false
                    showSuccess = true
                    delay(2000.milliseconds)
                    showSuccess = false
                    // Reset fields
                    cardNumber = ""
                    cardHolder = ""
                    expiryDate = ""
                    cvv = ""
                }
            }

            AnimatedVisibility(
                visible = showSuccess,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Success!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Payment method added successfully",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    darkTheme: Boolean
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (darkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = HoloCyan,
                unfocusedIndicatorColor = if (darkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                cursorColor = HoloCyan,
                focusedTextColor = if (darkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (darkTheme) Color.White else Color.Black
            ),
            singleLine = true
        )
    }
}

@Composable
fun SavedMethodItem(type: String, darkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, BorderWhite)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = if (type == "Visa") Color(0xFF1A1F71) else Color(0xFFEB001B)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (type == "Visa") "•••• •••• •••• 4242" else "•••• •••• •••• 5555",
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else Color.Black
                )
                Text(
                    text = type,
                    fontSize = 12.sp,
                    color = if (darkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(selected = type == "Visa", onClick = { /* TODO */ })
        }
    }
}

@Preview
@Composable
fun PaymentMethodScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        PaymentMethodScreen(onBackClick = {}, darkTheme = true)
    }
}
