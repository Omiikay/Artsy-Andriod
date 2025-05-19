package com.csci571.artsyapp.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.csci571.artsyapp.data.api.ApiClient
import com.csci571.artsyapp.data.model.ArtistDetail
import com.csci571.artsyapp.data.model.ArtistResult
import com.csci571.artsyapp.data.model.Artwork
import com.csci571.artsyapp.data.model.Category
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ArtsyRepository {
    private val api = ApiClient.getApiService()

    /*
     * This repository class handles all the API calls related to artists, artworks, and favorites.
     * It uses coroutines to perform network operations off the main thread.
     */
    suspend fun searchArtists(query: String): Resource<List<ArtistResult>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchArtists(query)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.results ?: emptyList())
            } else {
                Resource.Error("Failed to search artists: ${response.message()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches the details of a specific artist by their ID.
     * Returns a Resource object containing either the artist details or an error message.
     */
    suspend fun getArtistDetails(artistId: String): Resource<ArtistDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getArtistDetails(artistId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.artist!!)
            } else {
                Resource.Error("Failed to get artist details: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches a list of similar artists based on the given artist ID.
     * Returns a Resource object containing either the list of similar artists or an error message.
     */
    suspend fun getSimilarArtists(artistId: String): Resource<List<ArtistDetail>> = withContext(Dispatchers.IO) {
        try {
            delay(200)
            Log.d(TAG, "Getting similar artists for artistId: $artistId")
            val response = api.getSimilarArtists(artistId)
            Log.d(TAG, "Similar artists response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Resource.Success(response.body()?.similarArtists ?: emptyList())
            } else {
                Log.e(TAG, "Error getting similar artists: ${response.code()} - ${response.message()}")
                Resource.Error("Failed to get similar artists: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting similar artists", e)
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches a list of artworks for a specific artist by their ID.
     * Returns a Resource object containing either the list of artworks or an error message.
     */
    suspend fun getArtworks(artistId: String): Resource<List<Artwork>> = withContext(Dispatchers.IO) {
        try {
            delay(200)
            val response = api.getArtworks(artistId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.artworks ?: emptyList())
            } else {
                Resource.Error("Failed to get artworks: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches a list of categories for a specific artwork by its ID.
     * Returns a Resource object containing either the list of categories or an error message.
     */
    suspend fun getArtworkCategories(artworkId: String): Resource<List<Category>> = withContext(Dispatchers.IO) {
        try {
            delay(200)
            val response = api.getArtworkCategories(artworkId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.categories ?: emptyList())
            } else {
                Resource.Error("Failed to get artwork categories: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    /*
     * Fetches the list of favorite artists for the current user.
     * Returns a Resource object containing either the list of favorite artists or an error message.
     */
    suspend fun checkFavorite(artistId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkFavorite(artistId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.get("isFavorite") ?: false)
            } else {
                Resource.Error("Failed to check favorite status: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}