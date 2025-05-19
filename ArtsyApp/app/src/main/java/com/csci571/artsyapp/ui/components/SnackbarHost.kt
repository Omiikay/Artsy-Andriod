package com.csci571.artsyapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import kotlinx.coroutines.delay

@Composable
fun SnackbarHost(
    snackbarViewModel: SnackbarViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarData by snackbarViewModel.snackbarData.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        snackbarData?.let { data ->
            LaunchedEffect(data) {
                snackbarHostState.showSnackbar(
                    message = data.message,
                    actionLabel = "Dismiss",
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
                delay(3000)
                snackbarViewModel.dismissSnackbar()
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { snackbarData ->
                Snackbar(
                    containerColor =
                        if (data.isError) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.onSurface,
                    contentColor =
                        if (data.isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(snackbarData.visuals.message)
                }
            }
        }
    }
}