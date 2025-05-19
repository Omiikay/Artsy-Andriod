package com.csci571.artsyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.csci571.artsyapp.ui.components.ArtistCard
import com.csci571.artsyapp.ui.components.SearchBar
import com.csci571.artsyapp.ui.viewmodel.AuthViewModel
import com.csci571.artsyapp.ui.viewmodel.FavoritesViewModel
import com.csci571.artsyapp.ui.viewmodel.SearchViewModel
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import com.csci571.artsyapp.utils.Constants

@Composable
fun SearchResultScreen(
    query: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    favoritesViewModel: FavoritesViewModel,
    searchViewModel: SearchViewModel = viewModel(),
    snackbarViewModel: SnackbarViewModel = viewModel()
) {
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentQuery by searchViewModel.currentQuery.collectAsState()
    // 获取收藏的ID集合
    val favIdSet by favoritesViewModel.favoriteIds.collectAsState()

    LaunchedEffect(key1 = currentQuery) {
        if (currentQuery.length >= 3) {
            searchViewModel.searchArtists(currentQuery)
        }
    }

    // 新增：登录后马上拉一次 favorites
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            favoritesViewModel.getFavorites()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索结果页始终显示SearchBar
        SearchBar(
            initialQuery = currentQuery,
            onSearch = { newQuery ->
                // 直接执行搜索，无需限制字符数
                searchViewModel.searchArtists(newQuery)
            },
            onClose = {
                // 关闭搜索并返回主页
                navController.navigateUp()
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Column 负责将内部的元素水平居中
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp)) // 添加一些垂直间距
                    Text("Loading...")
                }
            }
        } else if (currentQuery.isEmpty()) {
            // 查询为空时，不显示任何结果
            Box(modifier = Modifier.fillMaxSize()) { /* 空白 */ }
        } else if (currentQuery.length >= 3 && searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "No result found.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { artist ->
                    ArtistCard(
                        imageUrl = artist.imageUrl,
                        title = artist.title,
                        id = artist.id,
                        isFavorite = favIdSet.contains(artist.id),
                        isLoggedIn = isLoggedIn,
                        onCardClick = {
                            navController.navigate("${Constants.Route.ARTIST_DETAIL}/${artist.id}")
                        },
                        onToggleFavorite = { artistId, isFavorite ->
                            if (isFavorite) {
                                favoritesViewModel.removeFavorite(artistId)
                                snackbarViewModel.showSnackbar("Removed from favorites")
                            } else {
                                favoritesViewModel.addFavorite(artistId)
                                snackbarViewModel.showSnackbar("Added to favorites")
                            }
                        }
                    )
                }
            }
        }
    }
}
