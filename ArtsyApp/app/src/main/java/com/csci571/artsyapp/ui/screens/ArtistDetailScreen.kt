package com.csci571.artsyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.csci571.artsyapp.data.model.ArtistDetail
import com.csci571.artsyapp.data.model.Artwork
import com.csci571.artsyapp.data.model.Category
import com.csci571.artsyapp.ui.components.ArtistCard
import com.csci571.artsyapp.ui.components.CategoryCarousel
import com.csci571.artsyapp.ui.viewmodel.ArtistDetailViewModel
import com.csci571.artsyapp.ui.viewmodel.AuthViewModel
import com.csci571.artsyapp.ui.viewmodel.FavoritesViewModel
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import com.csci571.artsyapp.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    favoritesViewModel: FavoritesViewModel,
    viewModel: ArtistDetailViewModel = viewModel(),
    snackbarViewModel: SnackbarViewModel = viewModel(),
) {
    val artist by viewModel.artist.collectAsState()
    val artworks by viewModel.artworks.collectAsState()
    val similarArtists by viewModel.similarArtists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingArtworks by viewModel.isLoadingArtworks.collectAsState()
    val isLoadingSimilar by viewModel.isLoadingSimilar.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val favIdSet by favoritesViewModel.favoriteIds.collectAsState()

    val isFavorite = favIdSet.contains(artistId)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = if (isLoggedIn) {
        listOf("Details", "Artworks", "Similar")
    } else {
        listOf("Details", "Artworks")
    }

    LaunchedEffect(artistId, isLoggedIn) {
        viewModel.getArtistDetails(artistId)
        viewModel.getArtworks(artistId)
        if (isLoggedIn) {
            viewModel.getSimilarArtists(artistId)
            favoritesViewModel.confirmSingleArtistStatus(artistId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(artist?.name ?: "Artist Details") }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }, actions = {
            if (isLoggedIn) {
                IconButton(
                    onClick = {
                        if (isFavorite) {
                            // viewModel.removeFavorite(artistId)
                            favoritesViewModel.removeFavorite(artistId)
                            snackbarViewModel.showSnackbar("Removed from favorites")
                        } else {
                            // viewModel.addFavorite(artistId)
                            favoritesViewModel.addFavorite(artistId)
                            snackbarViewModel.showSnackbar("Added to favorites")
                        }
                    }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
                    )
                }
            }
        })

        Column {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Outlined.Info, contentDescription = null)
                                1 -> Icon(Icons.Outlined.AccountBox, contentDescription = null)
                                2 -> Icon(Icons.Outlined.PersonSearch, contentDescription = null)
                            }
                        })
                }
            }

            when (selectedTabIndex) {
                0 -> DetailsTab(artist, isLoading)
                1 -> ArtworksTab(artworks, isLoadingArtworks)
                2 -> if (isLoggedIn) SimilarArtistsTab(
                    similarArtists = similarArtists,
                    isLoading = isLoadingSimilar,
                    onArtistClick = { similarArtistId ->
                        navController.navigate("${Constants.Route.ARTIST_DETAIL}/$similarArtistId")
                    },
                    onToggleFavorite = { artistId, isFavorite ->
                        if (isFavorite) {
                            favoritesViewModel.removeFavorite(artistId)
                            snackbarViewModel.showSnackbar("Removed from favorites")
                        } else {
                            favoritesViewModel.addFavorite(artistId)
                            snackbarViewModel.showSnackbar("Added to favorites")
                        }
                    },
                    favoritesViewModel = favoritesViewModel
                )
            }
        }
    }
}

@Composable
fun DetailsTab(artist: ArtistDetail?, isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // 仅在Details标签页下显示加载指示器
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...")
            }
        } else if (artist == null) {
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
                        text = "No artist details available",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        if (!artist.nationality.isNullOrEmpty()) {
                            Text(
                                text = artist.nationality + ", ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (!artist.birthday.isNullOrEmpty() || !artist.deathday.isNullOrEmpty()) {
                            Text(
                                text = buildString {
                                    append(artist.birthday ?: "")
                                    if (!artist.birthday.isNullOrEmpty() && !artist.deathday.isNullOrEmpty()) {
                                        append(" – ")
                                    }
                                    append(artist.deathday ?: "")
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    if (!artist.biography.isNullOrEmpty()) {
                        Text(
                            text = artist.biography,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtworksTab(artworks: List<Artwork>, isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // 仅在Artworks标签页下显示加载指示器
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...")
            }
        } else if (artworks.isEmpty()) {
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
                        text = "No artworks",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(artworks) { artwork ->
                ArtworkCard(artwork)
            }
        }
    }
}

@Composable
fun ArtworkCard(artwork: Artwork) {
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoadingCategories by remember { mutableStateOf(false) }
    val viewModel: ArtistDetailViewModel = viewModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 组合标题和日期为单个文本
                    val titleAndDate = buildString {
                        append(artwork.title)
                        if (!artwork.date.isNullOrEmpty()) {
                            // 只在日期不为空时追加逗号和日期
                            append(", ${artwork.date}")
                        }
                    }
                    Text(
                        text = titleAndDate,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showCategoriesDialog = true
                        isLoadingCategories = true
                        viewModel.getArtworkCategories(artwork.id) { fetchedCategories ->
                            categories = fetchedCategories
                            isLoadingCategories = false
                        }
                    }) {
                    Text("View categories")
                }
            }
        }
    }

    // 使用我们的新组件
    if (showCategoriesDialog) {
        CategoriesDialog(
            artwork = artwork,
            categories = categories,
            isLoading = isLoadingCategories,
            onDismiss = { showCategoriesDialog = false }
        )
    }
}

@Composable
fun CategoriesDialog(
    artwork: Artwork,
    categories: List<Category>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading...")
                        }
                    }
                } else if (categories.isEmpty()) {
                    Text(
                        text = "No categories",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // 使用抽取出的CategoryCarousel组件
                    CategoryCarousel(
                        categories = categories,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text("Close", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
fun SimilarArtistsTab(
    similarArtists: List<ArtistDetail>,
    isLoading: Boolean,
    onArtistClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // 仅在Similar标签页下显示加载指示
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...")
            }
        } else if (similarArtists.isEmpty()) {
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
                        text = "No similar artists",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            val favIdSet by favoritesViewModel.favoriteIds.collectAsState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(similarArtists) { artist ->
                    ArtistCard(
                        imageUrl = artist.imageUrl,
                        title = artist.name,
                        id = artist.id,
                        isFavorite = favIdSet.contains(artist.id),
                        isLoggedIn = true, // 此页面只在登录时可见，所以总是true
                        onCardClick = { onArtistClick(artist.id) },
                        onToggleFavorite = { artistId, isFavorite ->
                            onToggleFavorite(artistId, isFavorite)
                        }
                    )
                }
            }
        }
    }
}
