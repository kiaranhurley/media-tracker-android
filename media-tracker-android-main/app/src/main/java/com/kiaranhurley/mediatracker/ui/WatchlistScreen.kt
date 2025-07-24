package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    user: User,
    onFilmClick: (Int) -> Unit,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchListViewModel = hiltViewModel()
) {
    val watchlistState by viewModel.watchlistState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Movies", "Games")

    // Load watchlist when screen appears
    LaunchedEffect(user.userId) {
        viewModel.loadWatchlist(user.userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryGray, SurfaceGray)
                )
            )
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Watchlist",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarText
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBarBackground,
                titleContentColor = AppBarText
            )
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CardTitleText,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BrandOrange
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) BrandOrange else CardSubtitleText,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on state and selected tab
        when (val currentState = watchlistState) {
            is WatchListState.Loading -> {
                LoadingContent()
            }
            is WatchListState.Success -> {
                val filteredItems = when (selectedTab) {
                    0 -> currentState.watchlistItems // All
                    1 -> currentState.watchlistItems.filter { it.watchList.itemType == "FILM" } // Movies
                    2 -> currentState.watchlistItems.filter { it.watchList.itemType == "GAME" } // Games
                    else -> currentState.watchlistItems
                }

                if (filteredItems.isEmpty()) {
                    EmptyWatchlistContent(
                        selectedTab = selectedTab,
                        tabName = tabs[selectedTab]
                    )
                } else {
                    WatchlistContent(
                        watchlistItems = filteredItems,
                        onFilmClick = onFilmClick,
                        onGameClick = onGameClick,
                        onRemoveClick = { watchlistItem ->
                            viewModel.removeFromWatchlist(watchlistItem.watchList.watchListId)
                        }
                    )
                }
            }
            is WatchListState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadWatchlist(user.userId) }
                )
            }
            is WatchListState.Idle -> {
                LoadingContent()
            }
        }
    }
}

@Composable
private fun WatchlistContent(
    watchlistItems: List<WatchListItemWithContent>,
    onFilmClick: (Int) -> Unit,
    onGameClick: (Int) -> Unit,
    onRemoveClick: (WatchListItemWithContent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = watchlistItems,
            key = { it.watchList.watchListId }
        ) { watchlistItem ->
            WatchlistItemCard(
                watchlistItem = watchlistItem,
                onClick = {
                    if (watchlistItem.watchList.itemType == "FILM") {
                        onFilmClick(watchlistItem.watchList.itemId)
                    } else {
                        onGameClick(watchlistItem.watchList.itemId)
                    }
                },
                onRemoveClick = { onRemoveClick(watchlistItem) }
            )
        }
    }
}

@Composable
private fun WatchlistItemCard(
    watchlistItem: WatchListItemWithContent,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Content Image
            AsyncImage(
                model = watchlistItem.posterUrl,
                contentDescription = "${watchlistItem.title} poster",
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Content Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Content Type Badge
                Surface(
                    color = if (watchlistItem.watchList.itemType == "FILM") BrandBlue else BrandOrange,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = if (watchlistItem.watchList.itemType == "FILM") "MOVIE" else "GAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = watchlistItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Release info
                if (watchlistItem.releaseInfo != null) {
                    Text(
                        text = watchlistItem.releaseInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CardSubtitleText
                    )
                }

                Text(
                    text = "Added ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(watchlistItem.watchList.addedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardSubtitleText
                )
            }

            // Remove Button
            IconButton(
                onClick = onRemoveClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = StatusError
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from watchlist",
                    tint = StatusError
                )
            }
        }
    }
}

@Composable
private fun EmptyWatchlistContent(
    selectedTab: Int,
    tabName: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = when (selectedTab) {
                    1 -> Icons.Default.Movie
                    2 -> Icons.Default.SportsEsports
                    else -> Icons.Default.Movie
                },
                contentDescription = "Empty watchlist",
                tint = CardSubtitleText,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = when (selectedTab) {
                    0 -> "Your watchlist is empty"
                    1 -> "No movies in your watchlist"
                    2 -> "No games in your watchlist"
                    else -> "Your watchlist is empty"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = CardTitleText,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = when (selectedTab) {
                    0 -> "Start adding movies and games you want to watch or play!"
                    1 -> "Browse the film catalog and add movies you want to watch"
                    2 -> "Browse the game catalog and add games you want to play"
                    else -> "Start adding content you want to enjoy later"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = CardSubtitleText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = BrandOrange)
            Text(
                text = "Loading your watchlist...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error loading watchlist",
                style = MaterialTheme.typography.titleLarge,
                color = StatusError,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Retry")
            }
        }
    }
} 