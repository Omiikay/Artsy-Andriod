package com.csci571.artsyapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SearchBar(
    initialQuery: String = "",
    onSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    val focusRequester = remember { FocusRequester() }
    var debouncedQuery by remember { mutableStateOf(initialQuery) }

    // 添加防抖处理，避免每个字符都触发搜索
    LaunchedEffect(query) {
        when {
            query.isEmpty() -> {
                // 立即清空
                debouncedQuery = ""
            }
            query.length >= 3 -> {
                delay(300)
                debouncedQuery = query
            }
        }
    }

    // 只在延迟后的查询变化时搜索
    LaunchedEffect(debouncedQuery) {
//        if (debouncedQuery.length >= 3) {
//            onSearch(debouncedQuery)
//        }
        onSearch(debouncedQuery)
    }

    LaunchedEffect(key1 = true) {
        focusRequester.requestFocus()
    }

    TextField(
        value = query,
        onValueChange = {
            query = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // set height to 64dp as equal to TopAppBar
            .focusRequester(focusRequester),
        placeholder = { Text("Search artists...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (query.isNotEmpty()) {
                    onSearch(query)
                }
            }
        ),
        // sets focused indicator color to surface color
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor =  MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}