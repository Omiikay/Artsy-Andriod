package com.csci571.artsyapp.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csci571.artsyapp.ui.components.SnackbarHost
import com.csci571.artsyapp.ui.theme.ArtsyAppTheme
import com.csci571.artsyapp.ui.viewmodel.AuthViewModel
import com.csci571.artsyapp.ui.viewmodel.FavoritesViewModel
import com.csci571.artsyapp.ui.viewmodel.SnackbarViewModel
import com.csci571.artsyapp.utils.Constants
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtsyAppTheme {
                AppContent()
            }
        }
    }
}


@Composable
fun AppContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val snackbarViewModel: SnackbarViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    // 监听登录状态变化
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // 当登录状态变化时，刷新收藏列表
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            favoritesViewModel.getFavorites()
        }
    }

    LaunchedEffect(key1 = true) {
        authViewModel.checkLoginStatus()
        delay(2000) // 2 seconds delay for splash screen
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Constants.Route.HOME
        ) {
            composable(
                route = Constants.Route.HOME,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            ) {
                HomeScreen(
                    navController = navController,
                    snackbarViewModel = snackbarViewModel,
                    authViewModel = authViewModel,
                    favoritesViewModel = favoritesViewModel
                )
            }

            composable(Constants.Route.LOGIN) {
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }

            composable(Constants.Route.REGISTER) {
                RegisterScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            // Search screen
            composable(
                route = "${Constants.Route.SEARCH_RESULTS}/{query}",
                arguments = listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        defaultValue = "" // Default value for query
                        nullable = true // Allow null values
                    }
                )
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query") ?: ""
                SearchResultScreen(
                    query = query,
                    navController = navController,
                    authViewModel = authViewModel,
                    favoritesViewModel = favoritesViewModel,
                    snackbarViewModel = snackbarViewModel
                )
            }

            // Support for search results without query
            composable(
                route = Constants.Route.SEARCH_RESULTS
            ) {
                SearchResultScreen(
                    query = "",
                    navController = navController,
                    authViewModel = authViewModel,
                    favoritesViewModel = favoritesViewModel,
                    snackbarViewModel = snackbarViewModel
                )
            }

            // Artist detail screen
            composable(
                route = "${Constants.Route.ARTIST_DETAIL}/{artistId}",
                arguments = listOf(
                    navArgument("artistId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                ArtistDetailScreen(
                    artistId = artistId,
                    navController = navController,
                    authViewModel = authViewModel,
                    favoritesViewModel = favoritesViewModel,
                    snackbarViewModel = snackbarViewModel
                )
            }
        }

        // Global snackbar host
        SnackbarHost(snackbarViewModel = snackbarViewModel)
    }
}
