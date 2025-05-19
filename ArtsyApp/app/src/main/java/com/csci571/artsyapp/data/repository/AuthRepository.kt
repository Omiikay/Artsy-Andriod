package com.csci571.artsyapp.data.repository

import com.csci571.artsyapp.data.api.ApiClient
import com.csci571.artsyapp.data.model.LoginRequest
import com.csci571.artsyapp.data.model.RegisterRequest
import com.csci571.artsyapp.data.model.User
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val api = ApiClient.getApiService()

    /*
     * This repository class handles all the API calls related to user authentication.
     * It uses coroutines to perform network operations off the main thread.
     */
    suspend fun register(fullname: String, email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(RegisterRequest(fullname, email, password))
            if (response.isSuccessful) {
                Resource.Success(response.body()?.user!!)
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("email", ignoreCase = true) == true) {
                    // 直接返回包含"Email already exists"的错误信息，让AuthViewModel处理
                    Resource.Error("Email already exists")
                } else {
                    Resource.Error("Unknown Registration failed Reason: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Logs in a user with the provided email and password.
     * Returns a Resource object containing either the user data or an error message.
     */
    suspend fun login(email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Resource.Success(response.body()?.user!!)
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("password") == true || errorBody?.contains("email") == true) {
                    Resource.Error("Password or email is incorrect", mapOf("email" to "Password or email is incorrect"))
                } else {
                    Resource.Error("Unknown Login failed reason: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches the current logged-in user.
     * Returns a Resource object containing either the user data or an error message.
     */
    suspend fun getCurrentUser(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                Resource.Success(response.body()?.user!!)
            } else {
                Resource.Error("Failed to get current user: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Logs out the current user.
     * Returns a Resource object indicating success or failure.
     */
    suspend fun logout(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.logout()
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Logout failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Deletes the current user's account.
     * Returns a Resource object indicating success or failure.
     */
    suspend fun deleteAccount(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteAccount()
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Account deletion failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}