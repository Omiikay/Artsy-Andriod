package com.csci571.artsyapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.csci571.artsyapp.ui.viewmodel.AuthViewModel
import com.csci571.artsyapp.ui.viewmodel.FavoritesViewModel
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import com.csci571.artsyapp.data.model.Favorite
import com.csci571.artsyapp.utils.Constants
import com.csci571.artsyapp.utils.formatDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.csci571.artsyapp.utils.relativeTimeFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    favoritesViewModel: FavoritesViewModel,
    snackbarViewModel: SnackbarViewModel
) {
    val context = LocalContext.current
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val user by authViewModel.currentUser.collectAsState()
    val favorites by favoritesViewModel.favorites.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }
//    var showSearchBar by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            favoritesViewModel.getFavorites()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Artist Search") },
            actions = {
                // 点击搜索图标时，直接导航到搜索结果页面，初始查询为空字符串
                IconButton(onClick = {
                    navController.navigate("${Constants.Route.SEARCH_RESULTS}/")
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }

                if (isLoggedIn && user != null) {
                    AsyncImage(
                        model = user?.profileImageUrl,
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { showDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log Out") },
                            onClick = {
                                authViewModel.logout()
                                showDropdown = false
                                snackbarViewModel.showSnackbar("Logged out successfully")
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Account",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                authViewModel.deleteAccount()
                                showDropdown = false
                                snackbarViewModel.showSnackbar("Account deleted successfully")
                            }
                        )
                    }
                } else {
                    IconButton(onClick = { navController.navigate(Constants.Route.LOGIN) }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Login")
                    }
                }
            }
        )

        // Display current date
        Text(
            text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date()),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Favorites section
        Column(
            modifier = Modifier
                .fillMaxWidth(),
                // .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant), // light grey
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (isLoggedIn) {
                if (favorites.isEmpty()) {
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
                                text = "No favorites",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(favorites) { favorite ->
                            FavoriteItem(
                                favorite = favorite,
                                onClick = {
                                    navController.navigate("${Constants.Route.ARTIST_DETAIL}/${favorite.artistId}")
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { navController.navigate(Constants.Route.LOGIN) }) {
                        Text("Log in to see favorites")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Powered by Artsy",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.artsy.net".toUri())
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun FavoriteItem(
    favorite: Favorite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                text = favorite.artistName,
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = favorite.nationality,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (favorite.birthday.isEmpty()) "" else ", ${favorite.birthday}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        val relativeTime by relativeTimeFlow(favorite.addedAt)
            .collectAsState(initial = formatDateTime(favorite.addedAt))

        Text(
            text = relativeTime,
            style = MaterialTheme.typography.bodySmall
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Details",
            modifier = Modifier.size(24.dp)
        )
    }
}