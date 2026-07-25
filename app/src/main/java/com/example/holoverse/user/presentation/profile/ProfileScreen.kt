package com.example.holoverse.user.presentation.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.holoverse.R
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.HoloverseTheme
import com.example.holoverse.core.ui.theme.IbarraNovaFont

data class ProfileItemData(
    val icon: ImageVector,
    val title: String,
    val endText: String? = null,
    val titleColor: Color = Color.Unspecified,
    val iconColor: Color = Color.Gray,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onPaymentOptionClick: () -> Unit,
    onAdminClick: () -> Unit,
    onTermsAndConditionsClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onSignOutSuccess: () -> Unit,
    darkTheme: Boolean,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.updateLanguageName()
    }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignOutSuccess()
        }
    }

    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    val headerBrush = remember(darkTheme) { Brush(darkTheme) }

    val profileItems = listOf(
        ProfileItemData(
            Icons.Default.Person,
            stringResource(R.string.edit_profile),
            onClick = { onEditProfileClick() }),
        ProfileItemData(
            Icons.Default.Payment,
            stringResource(R.string.payment_option),
            onClick = { onPaymentOptionClick() }
        ),
        ProfileItemData(
            Icons.Default.Notifications,
            stringResource(R.string.notifications),
            onClick = {
                val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                }
                context.startActivity(intent)
            }
        ),
        ProfileItemData(Icons.Default.GppGood, stringResource(R.string.security)),
        ProfileItemData(
            Icons.Default.Language,
            stringResource(R.string.language),
            uiState.selectedLanguageName,
            onClick = { showLanguageSheet = true }
        ),
        ProfileItemData(
            Icons.Default.Analytics,
            stringResource(R.string.admin_control_panel),
            onClick = { onAdminClick() }
        ),
        ProfileItemData(
            Icons.Default.DarkMode,
            stringResource(R.string.dark_mode),
            uiState.selectedThemeMode.replaceFirstChar { it.uppercase() },
            onClick = { showThemeSheet = true }
        ),
        ProfileItemData(
            Icons.Default.Policy,
            stringResource(R.string.terms_conditions),
            onClick = { onTermsAndConditionsClick() }
        ),
        ProfileItemData(
            Icons.AutoMirrored.Filled.HelpOutline,
            stringResource(R.string.help_center),
            onClick = onHelpCenterClick
        ),
        ProfileItemData(
            Icons.AutoMirrored.Filled.Message,
            stringResource(R.string.invite_friends),
            onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.invite_message))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        ),
        ProfileItemData(
            Icons.AutoMirrored.Filled.Logout,
            stringResource(R.string.logout),
            titleColor = Color.Red,
            iconColor = Color.Red,
            onClick = { viewModel.signOut() }
        ),
    )
    var showBottomSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadProfileImage(it) }
        }
    )

    if (showBottomSheet) {
        ProfileImageBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onSeeImage = {
                showBottomSheet = false
                showFullScreenImage = true
            },
            onUploadImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                showBottomSheet = false
            }
        )
    }

    if (showLanguageSheet) {
        LanguageBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            onLanguageSelected = { code ->
                viewModel.onLanguageSelected(code)
                showLanguageSheet = false
            },
            selectedLanguageCode = uiState.selectedLanguageCode
        )
    }

    if (showThemeSheet) {
        ThemeBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            onThemeSelected = { mode ->
                viewModel.onThemeSelected(mode)
                showThemeSheet = false
            },
            selectedMode = uiState.selectedThemeMode
        )
    }

    if (showFullScreenImage && uiState.profileImageUrl != null) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uiState.profileImageUrl,
                    contentDescription = "Full Screen Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                    error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                )
                IconButton(
                    onClick = { showFullScreenImage = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    Scaffold { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(headerBrush)

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.profile),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = IbarraNovaFont
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(4.dp, Color(0xFF009688), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.profileImageUrl != null) {
                                    AsyncImage(
                                        model = uiState.profileImageUrl,
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.istockphoto_1934800957_612x612),
                                        error = painterResource(R.drawable.istockphoto_1934800957_612x612)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-8).dp, y = (-4).dp)
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier.clickable(onClick = {
                                        showBottomSheet = true
                                    }),
                                    contentDescription = "Edit Image",
                                    tint = Color(0xFF009688),
                                    imageVector = Icons.Default.Image,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            uiState.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            uiState.email,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    LazyColumn {
                        items(
                            count = profileItems.size,
                            key = { index -> profileItems[index].title },
                            contentType = { "profile_item" }
                        ) { index ->
                            val item = profileItems[index]
                            ProfileItem(item = item)
                            if (index < profileItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBottomSheet(
    onDismissRequest: () -> Unit,
    onThemeSelected: (String) -> Unit,
    selectedMode: String
) {
    val sheetState = rememberModalBottomSheetState()
    val themes = listOf(
        "light" to "Light",
        "dark" to "Dark",
        "system" to "System Default"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.dark_mode),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(themes) { (mode, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, modifier = Modifier.weight(1f), fontSize = 16.sp)
                        RadioButton(
                            selected = (selectedMode == mode),
                            onClick = { onThemeSelected(mode) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String?) -> Unit,
    selectedLanguageCode: String?
) {
    val sheetState = rememberModalBottomSheetState()
    val languages = listOf(
        null to stringResource(R.string.device_language),
        "en" to stringResource(R.string.english),
        "ar" to stringResource(R.string.arabic),
        "es" to stringResource(R.string.spanish),
        "it" to stringResource(R.string.italian)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(languages) { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, modifier = Modifier.weight(1f), fontSize = 16.sp)
                        RadioButton(
                            selected = (selectedLanguageCode == code),
                            onClick = { onLanguageSelected(code) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImageBottomSheet(
    onDismissRequest: () -> Unit,
    onSeeImage: () -> Unit,
    onUploadImage: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Profile Photo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeeImage() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF009688))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "See your Image", fontSize = 16.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUploadImage() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color(0xFF009688)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Upload a new image", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileItem(item: ProfileItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = item.title, tint = item.iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            item.title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = item.titleColor
        )
        item.endText?.let {
            Text(it, color = Color(0xFF007AFF), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    HoloverseTheme(darkTheme = true) {
        ProfileScreen(
            onEditProfileClick = {},
            onPaymentOptionClick = {},
            onAdminClick = {},
            onTermsAndConditionsClick = {},
            onHelpCenterClick = {},
            onSignOutSuccess = {},
            darkTheme = true,
        )
    }
}


