@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.holoverse.ui.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.R

@Composable
fun HomeSearchBar(
    onSearchClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSearchClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Text(
                    text = stringResource(R.string.search_for),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Surface(
                modifier = Modifier
                    .size(44.dp),
                onClick = onVoiceClick,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun SearchBarScaffoldSample() {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (active) 0.dp else 16.dp),
        query = query,
        onQueryChange = { query = it },
        onSearch = { active = false },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search") },
        leadingIcon = {
            if (active) {
                IconButton(onClick = { active = false }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
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
