@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.holoverse.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Removed unused imports:
// import androidx.compose.foundation.text.input.rememberTextFieldState
// import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
// import androidx.compose.material.icons.filled.AccountCircle
// import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
// import androidx.compose.material3.ExpandedFullScreenSearchBar
// import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
// import androidx.compose.material3.PlainTooltip
// import androidx.compose.material3.TooltipAnchorPosition
// import androidx.compose.material3.TooltipBox
// import androidx.compose.material3.TooltipDefaults
// import androidx.compose.material3.rememberTooltipState
// import androidx.compose.ui.input.nestedscroll.nestedScroll
// import androidx.compose.ui.semantics.clearAndSetSemantics
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.launch
// import androidx.compose.material3.rememberSearchBarState // State control is simpler with mutableStateOf
// import androidx.compose.material3.SearchBarDefaults // Not needed for simple SearchBar
// import androidx.compose.material3.SearchBarState
// import androidx.compose.material3.SearchBarValue

@Preview
@Composable
fun SearchBarScaffoldSample() {
    // 1. Standard approach: use 'query' for the text and 'active' for the expanded state
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val listItems = remember { List(100) { "Text $it" } }


    // 2. The main SearchBar component
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (active) 0.dp else 16.dp),
        query = query,
        onQueryChange = { query = it },
        onSearch = { active = false }, // Collapse search bar on search
        active = active,
        onActiveChange = { active = it }, // Toggle the expanded state
        placeholder = { Text("Search") },
        leadingIcon = {
            if (active) {
                // Back button when active (expanded)
                IconButton(onClick = { active = false }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                // Search icon when inactive (collapsed)
                Icon(Icons.Default.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic")
            }
        },

        ) {}
}


@Preview(showBackground = true, backgroundColor = 0x4F44FF)
@Composable
fun SearchBarSample() {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(0.85f)
            .height(48.dp)
            .background(
                shape = RoundedCornerShape(7.dp),
                color = MaterialTheme.colorScheme.surface
            ),

        verticalAlignment = Alignment.CenterVertically,

        ) {
        Spacer(modifier = Modifier.padding(5.dp))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search"
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            text = "Search for...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(0.80f)

        )
        Spacer(modifier = Modifier.padding(5.dp))

        Icon(
            imageVector = Icons.Default.KeyboardVoice,
            contentDescription = "Search"
        )


    }


}

@Preview(showBackground = true, backgroundColor = 0x4F44FF)
@Composable
fun SearchBarSampleV2() {
    var searchState by remember {
        mutableStateOf("Search for...")
    }
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(0.85f)
            .height(48.dp)
            .background(
                shape = RoundedCornerShape(7.dp),
                color = MaterialTheme.colorScheme.surface
            ),

        verticalAlignment = Alignment.CenterVertically,

        ) {
        Spacer(modifier = Modifier.padding(5.dp))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search"
        )
        Spacer(modifier = Modifier.padding(5.dp))
        BasicTextField(
            modifier = Modifier.fillMaxWidth(0.80f),
            value = searchState,
            onValueChange = {searchStateChange -> searchState = searchStateChange},
            singleLine = true

        )
        Spacer(modifier = Modifier.padding(5.dp))

        Icon(
            imageVector = Icons.Default.KeyboardVoice,
            contentDescription = "Search"
        )


    }


}




