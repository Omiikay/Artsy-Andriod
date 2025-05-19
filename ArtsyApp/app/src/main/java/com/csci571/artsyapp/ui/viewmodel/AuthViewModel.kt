package com.csci571.artsyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.artsyapp.data.model.User
import com.csci571.artsyapp.data.repository.AuthRepository
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<ErrorState?>(null)
    val error: StateFlow<ErrorState?> = _error

    init {
        checkLoginStatus()
    }

    fun register(fullname: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.register(fullname, email, password)) {
                is Resource.Success -> {
                    _currentUser.value = result.data
                    _isLoggedIn.value = true
                }
                is Resource.Error -> {
                    _error.value = ErrorState(result.message ?: "Registration failed", result.fieldErrors)
                }
            }

            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.login(email, password)) {
                is Resource.Success -> {
                    _currentUser.value = result.data
                    _isLoggedIn.value = true
                }
                is Resource.Error -> {
                    _error.value = ErrorState(result.message ?: "Login failed", result.fieldErrors)
                }
            }

            _isLoading.value = false
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getCurrentUser()) {
                is Resource.Success -> {
                    _currentUser.value = result.data
                    _isLoggedIn.value = true
                }
                is Resource.Error -> {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                }
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            when (repository.logout()) {
                is Resource.Success -> {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                }
                is Resource.Error -> {
                    // Handle error if needed
                }
            }

            _isLoading.value = false
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true

            when (repository.deleteAccount()) {
                is Resource.Success -> {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                }
                is Resource.Error -> {
                    // Handle error if needed
                }
            }

            _isLoading.value = false
        }
    }

    data class ErrorState(
        val message: String,
        val fieldErrors: Map<String, String>? = null
    )
}