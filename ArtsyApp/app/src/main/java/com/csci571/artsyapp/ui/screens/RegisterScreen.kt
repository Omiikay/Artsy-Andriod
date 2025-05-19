package com.csci571.artsyapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.csci571.artsyapp.ui.viewmodel.AuthViewModel
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import com.csci571.artsyapp.utils.Constants
import com.csci571.artsyapp.utils.isValidEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    snackbarViewModel: SnackbarViewModel = viewModel()
) {
    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullnameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Track focus state for validation on focus loss
    var fullnameFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorState by authViewModel.error.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Error state for backend errors
    var backendError by remember { mutableStateOf<String?>(null) }

    // Validation functions for each field
    fun validateFullname() {
        fullnameError = when {
            fullname.isEmpty() -> "Fullname cannot be empty"
            else -> null
        }
    }

    // Only check if field is empty when focus changes
    fun validateEmailEmpty() {
        emailError = if (email.isEmpty()) "Email cannot be empty" else null
    }

    // Full validation including format (only used when button is clicked)
    fun validateEmailFormat() {
        emailError = when {
            email.isEmpty() -> "Email cannot be empty"
            !isValidEmail(email) -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword() {
        passwordError = when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 4 -> "Password must be at least 4 characters"
            else -> null
        }
    }

    // 在email变化时重置邮箱相关的后端错误
    LaunchedEffect(email) {
        if (backendError != null && backendError?.contains("email", ignoreCase = true) == true) {
            backendError = null
        }
    }

    // 监听错误状态变化，并安全地更新本地状态
    LaunchedEffect(errorState) {
        // 这里我们不尝试访问errorState内部属性，而是直接判断它是否为null
        if (errorState != null) {
            // 获取一个安全的错误消息字符串
            val safeErrorMessage = try {
                // 使用反射尝试获取message属性值
                val messageField = errorState!!::class.java.getDeclaredField("message")
                messageField.isAccessible = true
                messageField.get(errorState) as? String ?: "Unknown error"
            } catch (e: Exception) {
                // 如果反射失败，将整个errorState转为字符串
                errorState.toString()
            }

            // 根据错误消息设置相应的UI状态
            if (safeErrorMessage.contains("email", ignoreCase = true)) {
                // 如果错误与邮箱相关，更新emailError
                emailError = "Email already exists"
            } else {
                // 其他错误更新到backendError
                backendError = safeErrorMessage
            }
        } else {
            // 错误状态为null时，清除后端错误
            backendError = null
        }
    }

    LaunchedEffect(key1 = isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Constants.Route.HOME) {
                popUpTo(Constants.Route.HOME) { inclusive = true }
            }
            snackbarViewModel.showSnackbar("Registered successfully")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Register") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = fullname,
                onValueChange = {
                    fullname = it
                    if (fullnameFocused) validateFullname()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val wasFocused = fullnameFocused
                        fullnameFocused = focusState.isFocused
                        if (wasFocused && !fullnameFocused) {
                            validateFullname()
                        }
                    },
                label = { Text("Full Name") },
                isError = fullnameError != null,
                supportingText = fullnameError?.let { { Text(it) } },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    // if (emailFocused) validateEmail()
                    // Clear error when user starts typing again
                    if (emailError != null) emailError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val wasFocused = emailFocused
                        emailFocused = focusState.isFocused
                        if (wasFocused && !emailFocused) {
                            // validateEmail()
                            // Only check if empty when focus changes
                            validateEmailEmpty()
                        }
                    },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordFocused) validatePassword()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val wasFocused = passwordFocused
                        passwordFocused = focusState.isFocused
                        if (wasFocused && !passwordFocused) {
                            validatePassword()
                        }
                    },
                label = { Text("Password") },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Validate fields
                    validateFullname()
                    validatePassword()
                    validateEmailFormat()

                    // 如果有任意错误，则不提交
                    if (emailError != null || passwordError != null) {
                        return@Button
                    }

                    // Submit registration
                    authViewModel.register(fullname, email, password)
                },
                enabled = !isLoading && fullname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
                        && fullnameError == null && emailError == null && passwordError == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 显示非邮箱相关的后端错误
            if (backendError != null && !backendError!!.contains("email", ignoreCase = true)) {
                Text(
                    text = backendError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ")
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Constants.Route.LOGIN)
                    }
                )
            }
        }
    }
}
