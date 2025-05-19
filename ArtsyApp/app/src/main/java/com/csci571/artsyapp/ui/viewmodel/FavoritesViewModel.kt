package com.csci571.artsyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci571.artsyapp.data.model.Favorite
import com.csci571.artsyapp.data.repository.FavoritesRepository
import com.csci571.artsyapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This ViewModel class handles the favorites functionality.
class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    fun getFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getFavorites()) {
                is Resource.Success -> {
                    _favorites.value = result.data

                    // 将获取到的收藏列表转换为ID集合
                    _favoriteIds.value = result.data.map { it.artistId }.toSet()
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _favorites.value = emptyList()

                    // 如果获取收藏失败，清空收藏ID列表
                    _favoriteIds.value = emptySet()
                }
            }

            _isLoading.value = false
        }
    }

    // Add favorite
    fun addFavorite(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // 乐观更新UI - 立即将ID添加到收藏集合中
            _favoriteIds.value += artistId

            // 调用添加收藏API
            when (val result = repository.addFavorite(artistId)) {
                is Resource.Success -> {
                    // 单独确认此艺术家的状态
                    confirmSingleArtistStatus(artistId)
                }
                is Resource.Error -> {
                    _error.value = result.message
                    // 恢复原状态
                    _favoriteIds.value -= artistId
                }
            }

            _isLoading.value = false
        }
    }

    // Remove favorite
    fun removeFavorite(artistId: String) {
        viewModelScope.launch {
            // 乐观更新UI - 立即从收藏集合中移除ID
            _favoriteIds.value -= artistId
            _isLoading.value = true
            _error.value = null

            // 调用移除收藏API
            when (val result = repository.removeFavorite(artistId)) {
                is Resource.Success -> {
                    // 单独确认此艺术家的状态
                    confirmSingleArtistStatus(artistId)
                }
                is Resource.Error -> {
                    _error.value = result.message
                    // 恢复原状态
                    _favoriteIds.value += artistId
                }
            }

            _isLoading.value = false
        }
    }

    // 新方法：只查询单个艺术家的收藏状态
    fun confirmSingleArtistStatus(artistId: String) {
        viewModelScope.launch {
            when (val result = repository.checkFavorite(artistId)) {
                is Resource.Success -> {
                    val isFavorite = result.data
                    val currentIds = _favoriteIds.value.toMutableSet()

                    if (isFavorite) {
                        currentIds.add(artistId)
                    } else {
                        currentIds.remove(artistId)
                    }

                    _favoriteIds.value = currentIds
                }
                is Resource.Error -> {
                    // 状态确认失败，但不做处理，因为已经乐观更新了UI
                    _error.value = result.message
                }
            }
        }
    }
}