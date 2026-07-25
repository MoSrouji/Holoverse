package com.example.holoverse.auth.presentation.common.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holoverse.auth.presentation.common.validation.state.ValidationState
import com.example.holoverse.auth.presentation.signup.SignUpTextFieldId
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.holoverse.core.ui.theme.GlassLight
import com.example.holoverse.core.ui.theme.BorderLight
import com.example.holoverse.core.ui.theme.HoloBlack
import com.example.holoverse.core.ui.theme.GlassWhite
import com.example.holoverse.core.ui.theme.BorderWhite
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.ui.theme.IbarraNovaNormalError13
import com.example.holoverse.core.ui.theme.IbarraNovaNormalGray14
import com.example.holoverse.core.ui.theme.IbarraNovaSemiBoldPlatinum16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableMenuScreen(
    state: ValidationState,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Select User Type") }
    val menuItems = listOf("Mentor", "Student")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "User Selection Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        RadioButtonMenu(
            isExpanded = isMenuExpanded,
            onToggle = { isMenuExpanded = !isMenuExpanded },
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
                isMenuExpanded = false
            },
            state = state,
            menuItems = menuItems

        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Selected: $selectedItem",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioButtonMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME),
    menuItems: List<String>,
    showIcon: Boolean = true,
    labelText: String? = null
) {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) GlassWhite else GlassLight
    val borderColor = if (darkTheme) BorderWhite else BorderLight
    val contentColor = if (darkTheme) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        if (labelText != null) {
            Text(
                text = labelText,
                style = IbarraNovaNormalGray14,
                color = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // Menu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(5.dp))
                .height(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { onToggle() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showIcon) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User type",
                            tint = HoloCyan,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Text(
                        text = selectedItem,
                        style = IbarraNovaNormalGray14,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse menu" else "Expand menu",
                    tint = HoloCyan
                )
            }
        }

        // Animated menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkTheme) HoloBlack.copy(alpha = 0.9f) else Color.White)
                    .border(
                        1.dp,
                        borderColor,
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            ) {
                menuItems.forEachIndexed { index, item ->
                    MenuItem(
                        text = item,
                        isLast = index == menuItems.size - 1,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
    if (state.hasError && state.errorMessageId != null) {
        Text(
            text = stringResource(id = state.errorMessageId),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 10.dp),
            style = IbarraNovaNormalError13,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItem(
    text: String,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val contentColor = if (darkTheme) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = contentColor,
            style = IbarraNovaSemiBoldPlatinum16
        )
    }

    // Divider (except for last item)
    if (!isLast) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(contentColor.copy(alpha = 0.1f))
        )
    }
}


@Composable
fun CheckBoxMenu(
    ifItEmptyText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    selectedItems: Set<String>,
    onItemSelected: (String) -> Unit,
    menuItems: List<String>,
    modifier: Modifier = Modifier,
    maxPreviewItems: Int = 1,
    labelText: String,
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME),
    selectedDotColor: Color = HoloCyan,
    unselectedDotColor: Color = Color.Transparent,
    dotBorderColor: Color = Color.White.copy(alpha = 0.5f),
    selectedTextColor: Color = HoloCyan,
    unselectedTextColor: Color = Color.White
) {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) GlassWhite else GlassLight
    val borderColor = if (darkTheme) BorderWhite else BorderLight
    val contentColor = if (darkTheme) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Header with chip preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(5.dp))
                .height(56.dp)
                .clickable { onToggle() }
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedItems.isEmpty()) {
                    Text(
                        text = ifItEmptyText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = contentColor.copy(alpha = 0.6f),
                        style = IbarraNovaSemiBoldPlatinum16
                    )
                } else {
                    // Show selected items preview
                    val previewItems = selectedItems.take(maxPreviewItems)
                    previewItems.forEachIndexed { index, item ->
                        Text(
                            text = if (index == previewItems.lastIndex) "$labelText $item" else " $item, ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = contentColor,
                            style = IbarraNovaSemiBoldPlatinum16,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Show "+X more" if more items selected
                    if (selectedItems.size > maxPreviewItems) {
                        Text(
                            text = " +${selectedItems.size - maxPreviewItems} more",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = HoloCyan,
                            style = IbarraNovaSemiBoldPlatinum16
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse menu" else "Expand menu",
                    tint = HoloCyan
                )
            }
        }

        // Dropdown items with improved selection indicator
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkTheme) HoloBlack.copy(alpha = 0.9f) else Color.White)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)
                    )
            ) {
                menuItems.forEach { item ->
                    val isSelected = selectedItems.contains(item)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .animateContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Improved selection indicator dot with animation
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isSelected) HoloCyan else Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.5.dp,
                                    color = if (isSelected) HoloCyan else contentColor.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) HoloCyan else contentColor,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = HoloCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Divider between items
                    if (item != menuItems.last()) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            color = if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(
                                alpha = 0.1f
                            ),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
    if (state.hasError && state.errorMessageId != null) {
        Text(
            text = stringResource(id = state.errorMessageId),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 10.dp),
            style = IbarraNovaNormalError13,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

// Preview Composable for testing
@Composable
@Preview
fun CheckBoxMenuPreview() {
    MaterialTheme {
        var isExpanded by remember { mutableStateOf(false) }
        var selectedItems by remember { mutableStateOf(setOf<String>()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CheckBoxMenu(
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded },
                selectedItems = selectedItems,
                onItemSelected = { item ->
                    selectedItems = if (selectedItems.contains(item)) {
                        selectedItems - item
                    } else {
                        selectedItems + item
                    }
                },
                menuItems = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5"),
                selectedDotColor = Color.Green,
                unselectedDotColor = Color.Transparent,
                dotBorderColor = Color.Gray,
                ifItEmptyText = " Select Option ",
                labelText = " label "

            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactExpandableMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME),
    menuItems: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Compact menu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .height(48.dp)
                .clickable { onToggle() }
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedItem,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                menuItems.forEach { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Error message
    if (state.hasError && state.errorMessageId != null) {
        Text(
            text = stringResource(id = state.errorMessageId),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerInput(
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME),
    label: String,
    showIcon: Boolean = true
) {
    // State to control the visibility of the DatePicker dialog
    val openDialog = rememberSaveable { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) GlassWhite else GlassLight
    val borderColor = if (darkTheme) BorderWhite else BorderLight
    val contentColor = if (darkTheme) Color.White else Color.Black

    // Convert Long timestamp to a readable date string
    val dateText = selectedDateMillis?.let {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
    } ?: label

    // --- Date Picker Dialog Implementation ---
    if (openDialog.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.YEAR, -18)
                    return utcTimeMillis <= calendar.timeInMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    return year <= currentYear - 18
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { openDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onDateSelected(datePickerState.selectedDateMillis)
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Custom Input Field ---
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Menu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(5.dp))
                .height(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { openDialog.value = true }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showIcon) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = HoloCyan,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Text(
                        text = dateText,
                        style = IbarraNovaNormalGray14,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedDateMillis != null) contentColor else contentColor.copy(
                            alpha = 0.6f
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Open Date Picker",
                    tint = HoloCyan
                )
            }
        }
    }

    // --- Error Message ---
    if (state.hasError && state.errorMessageId != null) {
        Text(
            text = stringResource(id = state.errorMessageId),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 10.dp),
            style = IbarraNovaNormalError13,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
//@Composable
//fun CompactMultiSelectMenu(
//    isExpanded: Boolean,
//    onToggle: () -> Unit,
//    selectedItems: Set<String>,
//    onItemSelected: (String) -> Unit, // Just toggles selection
//    menuItems: List<String>
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth(0.85f)
//            .clip(RoundedCornerShape(5.dp))
//    ) {
//        // Header with chip preview
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(MaterialTheme.colorScheme.background)
//                .height(56.dp)
//                .clickable { onToggle() }
//                .padding(horizontal = 12.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxSize(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                if (selectedItems.isEmpty()) {
//                    Text("Select options", style = MaterialTheme.typography.bodyMedium)
//                } else {
//                    // Show first selected item as preview
//                    Text(
//                        text = selectedItems.first(),
//                        style = MaterialTheme.typography.bodyMedium,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//
//                    // Show "+X more" if more items selected
//                    if (selectedItems.size > 1 ) {
//                        Text(
//                            text = " +${selectedItems.size - 1} more",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.weight(1f))
//
//                Icon(
//                    imageVector = Icons.Default.ArrowDropDown,
//                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//        }
//
//        // Dropdown items with selection indicator
//        AnimatedVisibility(visible = isExpanded) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                menuItems.forEach { item ->
//                    val isSelected = selectedItems.contains(item)
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { onItemSelected(item) }
//                            .padding(12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // Selection indicator dot
//                        Box(
//                            modifier = Modifier
//                                .size(8.dp)
//                                .background(
//                                    color = if (isSelected) MaterialTheme.colorScheme.primary
//                                    else Color.Transparent,
//                                    shape = CircleShape
//                                )
//                                .border(
//                                    width = 1.dp,
//                                    color = if (isSelected) MaterialTheme.colorScheme.primary
//                                    else MaterialTheme.colorScheme.outline,
//                                    shape = CircleShape
//                                )
//                        )
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Text(
//                            text = item,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = if (isSelected) MaterialTheme.colorScheme.primary
//                            else MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//

//@Preview
//@Composable
//fun ExpandableMenuPreview() {
//
//CompactMultiSelectMenu(
//    isExpanded = true,
//    onToggle = {},
//    selectedItems = setOf("Option 1", "Option 2"),
//    onItemSelected = {},
//    menuItems = listOf("Option 1", "Option 2", "Option 3")
//)
//}
//


