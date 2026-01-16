package com.example.holoverse.ui.commonPart.auth.widget

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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

import com.example.holoverse.ui.collectUserData.teacher.viewModels.SignUpTextField
import com.example.holoverse.ui.commonPart.auth.presentaiton.authentication.signup.SignUpTextFieldId
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState
import com.example.holoverse.ui.theme.IbarraNovaNormalError13
import com.example.holoverse.ui.theme.IbarraNovaNormalGray14
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum16
import com.google.android.libraries.places.api.model.LocalDate
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAccessor
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableMenuScreen(
    state: ValidationState,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Select User Type") }
    val menuItems = listOf("Teacher", "Student")

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
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME ),
    menuItems: List<String>,
    showIcon: Boolean = true
) {


    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Menu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Text(
                        text = selectedItem,
                        style = IbarraNovaNormalGray14,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        // color =  ColorPlatinum
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse menu" else "Expand menu",
                    tint = MaterialTheme.colorScheme.primary
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
    Column() {
        if (state.hasError && state.errorMessageId != null) {

            Text(
                text = stringResource(id = state.errorMessageId),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp),
                style = IbarraNovaNormalError13
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItem(
    text: String,
    isLast: Boolean,
    onClick: () -> Unit
) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = IbarraNovaSemiBoldPlatinum16
        )
    }

    // Divider (except for last item)
    if (!isLast) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
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
    state: ValidationState = ValidationState(id = SignUpTextFieldId.FULL_NAME ),
    selectedDotColor: Color = MaterialTheme.colorScheme.primary,
    unselectedDotColor: Color = Color.Transparent,
    dotBorderColor: Color = MaterialTheme.colorScheme.outline,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Header with chip preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = IbarraNovaSemiBoldPlatinum16,
                            //  color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse menu" else "Expand menu",
                    tint = MaterialTheme.colorScheme.primary


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
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
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
                                    color = if (isSelected) selectedDotColor else unselectedDotColor,
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.5.dp,
                                    color = if (isSelected) selectedDotColor else dotBorderColor,
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) selectedTextColor else unselectedTextColor,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = selectedDotColor,
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
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
    Column() {
        if (state.hasError && state.errorMessageId != null) {

            Text(
                text = stringResource(id = state.errorMessageId),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp),
                style = IbarraNovaNormalError13
            )
        }
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

    // Get the current context for the date picker state
    val context = LocalContext.current

    // Convert Long timestamp to a readable date string
    val dateText = selectedDateMillis?.let {
        // Use a simple date format for display
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
    } ?: label // Show the label when no date is selected

    // --- Date Picker Dialog Implementation ---
    if (openDialog.value) {
        val datePickerState = rememberDatePickerState(
            // Set the initial selection to the current selected date or null
            initialSelectedDateMillis = selectedDateMillis
        )

        // DatePicker dialog
        DatePickerDialog(
            onDismissRequest = { openDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onDateSelected(datePickerState.selectedDateMillis)
                    },
                    enabled = datePickerState.selectedDateMillis != null // Enable only when a date is selected
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

    // --- Custom Input Field (Themed to match RadioButtonMenu) ---
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Menu header (Simulates the clickable field)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .height(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { openDialog.value = true } // Open dialog on click
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
                            imageVector = Icons.Default.DateRange, // Using a date-related icon
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Text(
                        text = dateText,
                        style = if (selectedDateMillis != null) IbarraNovaNormalGray14 else MaterialTheme.typography.bodyLarge, // Adjust style based on selection
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedDateMillis != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday, // Calendar icon instead of dropdown arrow
                    contentDescription = "Open Date Picker",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

    }

    // --- Error Message (Themed to match RadioButtonMenu) ---
    Column() {
        if (state.hasError && state.errorMessageId != null) {
            Text(
                text = stringResource(id = state.errorMessageId),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp),
                style = IbarraNovaNormalError13
            )
        }
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