package com.example.holoverse.ui.commonPart.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF7F8FC)
                )
            )
        },
        containerColor = Color(0xFFF7F8FC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF009688), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // This would be an Image composable in a real app
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF009688)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = "Change Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            var fullName by remember { mutableStateOf("") }
            var nickName by remember { mutableStateOf("") }
            var dob by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("(+1) 724-848-1225") }
            var gender by remember { mutableStateOf("") }
            var studentStatus by remember { mutableStateOf("Student") }

            ProfileTextField(value = fullName, onValueChange = { fullName = it }, placeholder = "Full Name")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(value = nickName, onValueChange = { nickName = it }, placeholder = "Nick Name")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(
                value = dob,
                onValueChange = { dob = it },
                placeholder = "Date of Birth",
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PhoneTextField(value = phone, onValueChange = { phone = it })
            Spacer(modifier = Modifier.height(16.dp))
            GenderSpinner(selectedGender = gender, onGenderSelected = { gender = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(value = studentStatus, onValueChange = { studentStatus = it }, placeholder = "Student")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* Handle update */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D69FF))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Update", fontSize = 18.sp, color = Color.White)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    trailingIcon()
                }
            }
        }
    )
}


@Composable
fun PhoneTextField(value: String, onValueChange: (String) -> Unit) {
    var countryDialog by remember { mutableStateOf(false) }

    if (countryDialog) {
        Dialog(onDismissRequest = { countryDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Country", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🇺🇸 United States (+1)", modifier = Modifier.clickable { countryDialog = false })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🇬🇧 United Kingdom (+44)", modifier = Modifier.clickable { countryDialog = false })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🇮🇳 India (+91)", modifier = Modifier.clickable { countryDialog = false })
                }
            }
        }
    }

    ProfileTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "Phone number",
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { countryDialog = true }
            ) {
                Text("🇺🇸", fontSize = 24.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            }
        }
    )
}

@Composable
fun GenderSpinner(selectedGender: String, onGenderSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female", "Other")

    Box {
        ProfileTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            placeholder = "Gender",
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.Gray) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(Color.White)
        ) {
            genders.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F8FC)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen()
}
