package com.csci571.artsyapp.utils

/** Resource.kt
 *
 * This file defines a sealed class Resource that represents the result of an operation.
 * It can be either a success with data or an error with an optional message and field errors.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String? = null,
        val fieldErrors: Map<String, String>? = null
    ) : Resource<Nothing>()
}
