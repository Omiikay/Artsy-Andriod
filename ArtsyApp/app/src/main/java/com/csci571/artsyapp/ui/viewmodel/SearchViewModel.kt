package com.csci571.artsyapp.ui.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.artsyapp.data.model.ArtistResult
import com.csci571.artsyapp.data.repository.ArtsyRepository
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = ArtsyRepository()

    private val _searchResults = MutableStateFlow<List<ArtistResult>>(emptyList())
    val searchResults: StateFlow<List<ArtistResult>> = _searchResults

    // Save the current search queries
    private val _currentQuery = MutableStateFlow<String>("")
    val currentQuery: StateFlow<String> = _currentQuery

    // Loading state to indicate whether a search is in progress
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favorites = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favorites: StateFlow<Map<String, Boolean>> = _favorites

    private var searchJob: Job? = null
    private var favoritesJob: Job? = null

    /** This ViewModel class handles the search functionality for artists.
     * It uses coroutines to perform network operations off the main thread.
     * The search results, loading state, and error messages are exposed as StateFlow objects.
     */
    fun searchArtists(query: String) {
        // Update the current query
        _currentQuery.value = query

        // 新增：当查询为空时，直接清空结果并返回
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isLoading.value = false
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Add a small delay to prevent too many API calls while typing
            delay(300)

            try {
                when (val result = repository.searchArtists(query)) {
                    is Resource.Success -> {
                        _searchResults.value = result.data
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                        _searchResults.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "An unexpected error occurred: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Fetches the favorite status of artists.
     * This function is called after the search results are obtained.
     */
    fun getFavoriteStatus(artists: List<ArtistResult>) {
        // Cancel the previous job if it's still running
        favoritesJob?.cancel()

        // If there are no artists, return early
        if (artists.isEmpty()) {
            Log.d(TAG, "No artists to check favorite status")
            return
        }

        favoritesJob =  viewModelScope.launch {
            val favoritesMap = mutableMapOf<String, Boolean>()
            // Save the current favorites to avoid unnecessary API calls
            val currentFavorites = _favorites.value.toMutableMap()

            artists.forEach { artist ->
                // Check if the artist is already in the current favorites to avoid unnecessary API calls
                if (currentFavorites.containsKey(artist.id)) {
                    favoritesMap[artist.id] = currentFavorites[artist.id] ?: false
                } else {
                    when (val result = repository.checkFavorite(artist.id)) {
                        is Resource.Success -> {
                            favoritesMap[artist.id] = result.data
                        }
                        is Resource.Error -> {
                            // Default to not favorite if error
                            favoritesMap[artist.id] = false
                        }
                    }
                }
            }

            _favorites.value = favoritesMap
        }
    }
}