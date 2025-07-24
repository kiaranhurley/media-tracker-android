package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Films : Screen("films", "Films", Icons.Default.Movie)
    object Games : Screen("games", "Games", Icons.Default.SportsEsports)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.BookmarkBorder)
    object CustomLists : Screen("custom_lists", "Lists", Icons.Default.List)
    object Reviews : Screen("reviews", "Reviews", Icons.Default.RateReview)
    object Ratings : Screen("ratings", "Ratings", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    
    // Detail screens with parameters
    object FilmDetail {
        const val route = "film_detail/{filmId}"
        fun createRoute(filmId: Int) = "film_detail/$filmId"
    }
    object GameDetail {
        const val route = "game_detail/{gameId}" 
        fun createRoute(gameId: Int) = "game_detail/$gameId"
    }
}

// Add route for custom list detail
object CustomListDetail {
    const val route = "custom_list_detail/{listId}"
    fun createRoute(listId: Int) = "custom_list_detail/$listId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Films, Screen.Games, Screen.Search, Screen.Profile)
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = AppBarBackground,
                contentColor = AppBarText
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            // Force navigation to the target screen
                            navController.navigate(screen.route) {
                                // Clear the entire back stack
                                popUpTo(0) {
                                    inclusive = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandOrange,
                            selectedTextColor = AppBarText,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = BrandOrange.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main screens
            composable(Screen.Home.route) {
                HomeScreen(
                    user = user,
                    onLogout = onLogout,
                    onNavigateToWatchlist = {
                        navController.navigate(Screen.Watchlist.route)
                    }
                )
            }
            
            composable(Screen.Films.route) {
                FilmCatalogScreen(
                    user = user,
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    }
                )
            }
            
            composable(Screen.Games.route) {
                GameCatalogScreen(
                    user = user,
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }
            
            composable(Screen.Search.route) {
                SearchScreen(
                    user = user,
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = user,
                    onLogout = onLogout,
                    onNavigateToWatchlist = {
                        navController.navigate(Screen.Watchlist.route)
                    },
                    onNavigateToCustomLists = {
                        navController.navigate(Screen.CustomLists.route)
                    },
                    navController = navController // <-- pass navController
                )
            }
            
            // Detail screens - these will be on top of the main navigation
            composable(
                route = Screen.FilmDetail.route,
                arguments = listOf(navArgument("filmId") { type = NavType.IntType })
            ) { backStackEntry ->
                val filmId = backStackEntry.arguments?.getInt("filmId") ?: return@composable
                FilmDetailScreen(
                    filmId = filmId,
                    user = user,
                    onBackClick = { 
                        // Navigate back to the previous screen
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.GameDetail.route,
                arguments = listOf(navArgument("gameId") { type = NavType.IntType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
                GameDetailScreen(
                    gameId = gameId,
                    user = user,
                    onBackClick = { 
                        // Navigate back to the previous screen
                        navController.popBackStack()
                    }
                )
            }
            
            // Secondary screens
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    user = user,
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }
            
            composable(Screen.CustomLists.route) {
                CustomListScreen(
                    user = user,
                    onListClick = { listId ->
                        // Could navigate to list detail screen in the future
                    }
                )
            }

            composable(Screen.Reviews.route) {
                ReviewsScreen(
                    user = user,
                    onBackClick = { 
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Ratings.route) {
                RatingsScreen(
                    user = user,
                    onBackClick = { 
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    user = user,
                    onBackClick = { 
                        navController.popBackStack()
                    },
                    onLogout = {
                        // Clear user data and navigate to auth screen
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = CustomListDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.IntType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getInt("listId") ?: return@composable
                CustomListDetailScreen(listId = listId, user = user)
            }
        }
    }
} 