package com.csci571.artsyapp.data.repository

import com.csci571.artsyapp.data.api.ApiClient
import com.csci571.artsyapp.data.model.Favorite
import com.csci571.artsyapp.data.model.FavoriteRequest
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoritesRepository {
    private val api = ApiClient.getApiService()

    /*
     * This repository class handles all the API calls related to favorites.
     * It uses coroutines to perform network operations off the main thread.
     */
    suspend fun getFavorites(): Resource<List<Favorite>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFavorites()
            if (response.isSuccessful) {
                Resource.Success(response.body()?.favorites ?: emptyList())
            } else {
                Resource.Error("Failed to get favorites: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
 * Fetches the list of favorite artists for the current user.
 * Returns a Resource object containing either the list of favorite artists or an error message.
 */
    suspend fun addFavorite(artistId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.addFavorite(FavoriteRequest(artistId))
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to add to favorites: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Removes an artist from the user's favorites list.
     * Returns a Resource object containing either a success message or an error message.
     */
    suspend fun removeFavorite(artistId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.removeFavorite(artistId)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to remove from favorites: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Checks if an artist is in the user's favorites list.
     * Returns a Resource object containing either the favorite status or an error message.
     */
    suspend fun checkFavorite(artistId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkFavorite(artistId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.get("isFavorite") == true)
            } else {
                Resource.Error("Failed to check favorite status: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}