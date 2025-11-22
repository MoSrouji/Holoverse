package com.example.holoverse.ui.auth.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.holoverse.ui.auth.validation.state.ValidationState
import com.example.holoverse.ui.theme.IbarraNovaNormalError13
import com.example.holoverse.ui.theme.IbarraNovaNormalGray14
import com.example.holoverse.ui.theme.IbarraNovaSemiBoldPlatinum16


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableMenuScreen(
    state: ValidationState,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Select User Type") }

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

        ExpandableMenu(
            isExpanded = isMenuExpanded,
            onToggle = { isMenuExpanded = !isMenuExpanded },
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
                isMenuExpanded = false
            },
            state = state

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
fun ExpandableMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    state: ValidationState
    ) {
    val menuItems = listOf("Teacher", "Student")

    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Menu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background).height(56.dp)
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User type",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
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