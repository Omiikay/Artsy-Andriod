package com.csci571.artsyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.artsyapp.data.model.ArtistDetail
import com.csci571.artsyapp.data.model.Artwork
import com.csci571.artsyapp.data.model.Category
import com.csci571.artsyapp.data.repository.ArtsyRepository
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistDetailViewModel : ViewModel() {
    private val repository = ArtsyRepository()

    private val _artist = MutableStateFlow<ArtistDetail?>(null)
    val artist: StateFlow<ArtistDetail?> = _artist

    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList())
    val artworks: StateFlow<List<Artwork>> = _artworks

    private val _similarArtists = MutableStateFlow<List<ArtistDetail>>(emptyList())
    val similarArtists: StateFlow<List<ArtistDetail>> = _similarArtists

    // Loading state to indicate whether data is being loaded
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Loading state to indicate whether artworks are being loaded
    private val _isLoadingArtworks = MutableStateFlow(false)
    val isLoadingArtworks: StateFlow<Boolean> = _isLoadingArtworks

    // Loading state to indicate whether similar artists are being loaded
    private val _isLoadingSimilar = MutableStateFlow(false)
    val isLoadingSimilar: StateFlow<Boolean> = _isLoadingSimilar

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _favoritesMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoritesMap: StateFlow<Map<String, Boolean>> = _favoritesMap

    /*
     * This ViewModel class handles the details of a specific artist.
     * It uses coroutines to perform network operations off the main thread.
     * The artist details, artworks, similar artists, loading state, and error messages are exposed as StateFlow objects.
     */
    fun getArtistDetails(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getArtistDetails(artistId)) {
                is Resource.Success -> {
                    _artist.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /*
     * Fetches the artworks of a specific artist by their ID.
     * Returns a Resource object containing either the list of artworks or an error message.
     */
    fun getArtworks(artistId: String) {
        viewModelScope.launch {
            _isLoadingArtworks.value = true

            when (val result = repository.getArtworks(artistId)) {
                is Resource.Success -> {
                    _artworks.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _artworks.value = emptyList()
                }
            }

            _isLoadingArtworks.value = false
        }
    }

    /*
     * Fetches a list of similar artists based on the given artist ID.
     * Returns a Resource object containing either the list of similar artists or an error message.
     */
    fun getSimilarArtists(artistId: String) {
        viewModelScope.launch {
            _isLoadingSimilar.value = true

            when (val result = repository.getSimilarArtists(artistId)) {
                is Resource.Success -> {
                    _similarArtists.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _similarArtists.value = emptyList()
                }
            }

            _isLoadingSimilar.value = false
        }
    }

    /*
     * Fetches the categories of a specific artwork by its ID.
     * Returns a Resource object containing either the list of categories or an error message.
     */
    fun getArtworkCategories(artworkId: String, onComplete: (List<Category>) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.getArtworkCategories(artworkId)) {
                is Resource.Success -> {
                    onComplete(result.data)
                }
                is Resource.Error -> {
                    _error.value = result.message
                    onComplete(emptyList())
                }
            }
        }
    }

    /*
     * Checks if the artist is a favorite.
     * This function is called after the artist details are obtained.
     */
    fun checkFavoriteStatus(artistId: String) {
        viewModelScope.launch {
            when (val result = repository.checkFavorite(artistId)) {
                is Resource.Success -> {
                    _isFavorite.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _isFavorite.value = false
                }
            }
        }
    }

    /*
     * Fetches the favorite status of multiple artists.
     * This function is called after the artist details are obtained.
     */
    fun checkFavoritesStatuses(artistIds: List<String>) {
        viewModelScope.launch {
            val favoritesMap = mutableMapOf<String, Boolean>()

            artistIds.forEach { artistId ->
                when (val result = repository.checkFavorite(artistId)) {
                    is Resource.Success -> {
                        favoritesMap[artistId] = result.data
                    }
                    is Resource.Error -> {
                        favoritesMap[artistId] = false
                    }
                }
            }

            _favoritesMap.value = favoritesMap
        }
    }

    /*
//     * Adds an artist to the favorites list.
//     * This function is called when the user marks an artist as favorite.
//     */
//    fun addFavorite(artistId: String) {
//        viewModelScope.launch {
//            when (val result = repository.addFavorite(artistId)) {
//                is Resource.Success -> {
//                    _isFavorite.value = true
//
//                    // Update favorites map if needed
//                    if (_favoritesMap.value.containsKey(artistId)) {
//                        val updatedMap = _favoritesMap.value.toMutableMap()
//                        updatedMap[artistId] = true
//                        _favoritesMap.value = updatedMap
//                    }
//                }
//                is Resource.Error -> {
//                    _error.value = result.message
//                }
//            }
//        }
//    }
//
//    /*
//     * Removes an artist from the favorites list.
//     * This function is called when the user unmarks an artist as favorite.
//     */
//    fun removeFavorite(artistId: String) {
//        viewModelScope.launch {
//            when (val result = repository.removeFavorite(artistId)) {
//                is Resource.Success -> {
//                    _isFavorite.value = false
//
//                    // Update favorites map if needed
//                    if (_favoritesMap.value.containsKey(artistId)) {
//                        val updatedMap = _favoritesMap.value.toMutableMap()
//                        updatedMap[artistId] = false
//                        _favoritesMap.value = updatedMap
//                    }
//                }
//                is Resource.Error -> {
//                    _error.value = result.message
//                }
//            }
//        }
//    }
}