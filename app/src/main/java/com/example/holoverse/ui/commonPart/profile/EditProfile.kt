package com.example.holoverse.ui.commonpart.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.commonpart.auth.util.TextFieldType
import com.example.holoverse.ui.commonpart.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.commonpart.auth.validation.event.ValidationResultEvent
import com.example.holoverse.ui.commonpart.auth.widget.DatePickerInput
import com.example.holoverse.ui.commonpart.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.commonpart.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.commonpart.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.ui.spatialtheme.Brush
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaFont
import com.example.holoverse.utils.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onUpdateSuccess: () -> Unit,
    darkTheme: Boolean = true,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val editProfileState by viewModel.editProfileState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val genderItems = listOf("Male", "Female")
    var isGenderMenuExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.selectedImageUri = uri
    }

    LaunchedEffect(editProfileState) {
        when (editProfileState) {
            is Response.Success -> {
                if ((editProfileState as Response.Success<Boolean>).data) {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onUpdateSuccess()
                }
            }
            is Response.Error -> {
                Toast.makeText(context, (editProfileState as Response.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush(darkTheme))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Profile",
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbarraNovaFont,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .border(3.dp, Color(0xFF009688), CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = viewModel.selectedImageUri
                        ?: when (val user = currentUser) {
                            is User.Student -> user.profileImageUrl
                            is User.Mentor -> user.profileImageUrl
                            else -> null
                        }

                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                            error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Photo",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Fields
                viewModel.forms[EditProfileTextFieldId.FULL_NAME]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.full_name,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Text
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.forms[EditProfileTextFieldId.EMAIL]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.email,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Email
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Change Password Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .clickable { onChangePasswordClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility_on),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.change_password),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentUser is User.Mentor) {
                    viewModel.forms[EditProfileTextFieldId.BIO]?.let { state ->
                        AuthenticationTextField(
                            modifier = Modifier.fillMaxWidth(),
                            state = state,
                            hint = R.string.bio,
                            onValueChange = {
                                viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                            },
                            type = TextFieldType.Text
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                viewModel.forms[EditProfileTextFieldId.PHONE_NUMBER]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.phoneNumber,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.PhoneNumber,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.forms[EditProfileTextFieldId.ADDRESS]?.let { state ->
                    AuthenticationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        hint = R.string.address,
                        onValueChange = {
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = it)))
                        },
                        type = TextFieldType.Text
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.forms[EditProfileTextFieldId.DATE_OF_BIRTH]?.let { state ->
                    val locale = androidx.compose.ui.text.intl.Locale.current.platformLocale
                    val selectedMillis = try {
                        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", locale)
                        sdf.parse(state.text)?.time
                    } catch (_: Exception) {
                        null
                    }

                    DatePickerInput(
                        selectedDateMillis = selectedMillis,
                        onDateSelected = { millis ->
                            millis?.let { viewModel.onDateSelected(it) }
                        },
                        state = state,
                        label = "Date of Birth",
                        showIcon = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.forms[EditProfileTextFieldId.GENDER]?.let { state ->
                    RadioButtonMenu(
                        isExpanded = isGenderMenuExpanded,
                        onToggle = { isGenderMenuExpanded = !isGenderMenuExpanded },
                        selectedItem = viewModel.selectedGender,
                        onItemSelected = { item ->
                            viewModel.selectedGender = item
                            isGenderMenuExpanded = false
                            viewModel.onEvent(ValidationEvent.TextFieldValueChange(state.copy(text = item)))
                        },
                        state = state,
                        menuItems = genderItems,
                        labelText = "Gender :"
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                AuthenticationButton(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    textId = R.string.update,
                    onClick = {
                        viewModel.onEvent(ValidationEvent.Submit)
                    }
                )
                
                // Note: The ValidationResultEvent.Success collection in ViewModel or here should trigger updateProfile()
            }

            if (editProfileState is Response.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
    
    // Listen for validation success to trigger update
    LaunchedEffect(Unit) {
        viewModel.validationEvent.collect { event ->
            if (event is ValidationResultEvent.Success) {
                viewModel.updateProfile()
            }
        }
    }
}
