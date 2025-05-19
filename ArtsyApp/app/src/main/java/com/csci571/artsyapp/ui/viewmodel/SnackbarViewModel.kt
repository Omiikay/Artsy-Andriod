package com.csci571.artsyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This ViewModel class handles the Snackbar functionality.
class SnackbarViewModel : ViewModel() {
    private val _snackbarData = MutableStateFlow<SnackbarData?>(null)
    val snackbarData: StateFlow<SnackbarData?> = _snackbarData

    fun showSnackbar(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _snackbarData.value = SnackbarData(message, isError)
        }
    }

    fun dismissSnackbar() {
        viewModelScope.launch {
            _snackbarData.value = null
        }
    }

    data class SnackbarData(
        val message: String,
        val isError: Boolean = false
    )
}