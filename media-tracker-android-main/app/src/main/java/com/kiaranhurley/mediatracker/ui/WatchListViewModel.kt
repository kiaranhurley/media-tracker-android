package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.repository.WatchListRepository
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold watchlist item with content details
data class WatchListItemWithContent(
    val watchList: WatchList,
    val title: String,
    val posterUrl: String?,
    val releaseInfo: String? // Release year for films, release date for games
)

sealed class WatchListState {
    object Idle : WatchListState()
    object Loading : WatchListState()
    data class Success(val watchlistItems: List<WatchListItemWithContent>) : WatchListState()
    data class Error(val message: String) : WatchListState()
}

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val watchListRepository: WatchListRepository,
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    
    private val _watchlistState = MutableStateFlow<WatchListState>(WatchListState.Idle)
    val watchlistState: StateFlow<WatchListState> = _watchlistState.asStateFlow()

    fun loadWatchlist(userId: Int) {
        _watchlistState.value = WatchListState.Loading
        viewModelScope.launch {
            try {
                val watchlistItems = watchListRepository.getUserWatchList(userId).firstOrNull() ?: emptyList()
                println("DEBUG: WatchList - Found ${watchlistItems.size} raw watchlist items for user $userId")
                
                // Get content details for each watchlist item
                val itemsWithContent = watchlistItems.map { watchlistItem ->
                    when (watchlistItem.itemType) {
                        "FILM" -> {
                            var film = filmRepository.getFilmById(watchlistItem.itemId)
                            
                            // If not found by local ID, try by TMDB ID
                            if (film == null) {
                                film = filmRepository.getFilmByTmdbId(watchlistItem.itemId)
                            }
                            
                            // If still not found, try to fetch from API
                            if (film == null) {
                                println("DEBUG: WatchList - Attempting to fetch film ${watchlistItem.itemId} from TMDB API")
                                film = filmRepository.getFilmDetailsFromApi(watchlistItem.itemId)
                            }
                            
                            if (film != null) {
                                WatchListItemWithContent(
                                    watchList = watchlistItem,
                                    title = film.title,
                                    posterUrl = if (!film.posterPath.isNullOrBlank()) {
                                        "https://image.tmdb.org/t/p/w500${film.posterPath}"
                                    } else null,
                                    releaseInfo = film.releaseDate?.let { releaseDate ->
                                        // Format date to year
                                        try {
                                            java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
                                                .format(releaseDate)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                )
                            } else {
                                // Film not found anywhere, create fallback entry
                                println("DEBUG: WatchList - Film with ID ${watchlistItem.itemId} not found in local database or API")
                                WatchListItemWithContent(
                                    watchList = watchlistItem,
                                    title = "Unknown Film (ID: ${watchlistItem.itemId})",
                                    posterUrl = null,
                                    releaseInfo = null
                                )
                            }
                        }
                        "GAME" -> {
                            var game = gameRepository.getGameById(watchlistItem.itemId)
                            
                            // If not found by local ID, try by IGDB ID
                            if (game == null) {
                                game = gameRepository.getGameByIgdbId(watchlistItem.itemId)
                            }
                            
                            // If still not found, try to fetch from API
                            if (game == null) {
                                println("DEBUG: WatchList - Attempting to fetch game ${watchlistItem.itemId} from IGDB API")
                                game = gameRepository.getGameDetailsFromApi(watchlistItem.itemId)
                            }
                            
                            if (game != null) {
                                WatchListItemWithContent(
                                    watchList = watchlistItem,
                                    title = game.name,
                                    posterUrl = game.coverUrl,
                                    releaseInfo = game.firstReleaseDate?.let { date ->
                                        // Convert date to year
                                        java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
                                            .format(date)
                                    }
                                )
                            } else {
                                // Game not found anywhere, create fallback entry
                                println("DEBUG: WatchList - Game with ID ${watchlistItem.itemId} not found in local database or API")
                                WatchListItemWithContent(
                                    watchList = watchlistItem,
                                    title = "Unknown Game (ID: ${watchlistItem.itemId})",
                                    posterUrl = null,
                                    releaseInfo = null
                                )
                            }
                        }
                        else -> {
                            // Unknown item type, create fallback entry
                            println("DEBUG: WatchList - Unknown item type: ${watchlistItem.itemType}")
                            WatchListItemWithContent(
                                watchList = watchlistItem,
                                title = "Unknown Item (${watchlistItem.itemType})",
                                posterUrl = null,
                                releaseInfo = null
                            )
                        }
                    }
                }
                
                println("DEBUG: WatchList - Successfully created ${itemsWithContent.size} content items")
                _watchlistState.value = WatchListState.Success(itemsWithContent)
            } catch (e: Exception) {
                println("DEBUG: WatchList - Error loading watchlist: ${e.message}")
                e.printStackTrace()
                _watchlistState.value = WatchListState.Error(e.message ?: "Failed to load watchlist")
            }
        }
    }

    fun removeFromWatchlist(watchListId: Int) {
        viewModelScope.launch {
            try {
                // We need to find the watchlist item to remove it
                val currentState = _watchlistState.value
                if (currentState is WatchListState.Success) {
                    val itemToRemove = currentState.watchlistItems.find { it.watchList.watchListId == watchListId }
                    itemToRemove?.let {
                        watchListRepository.removeFromWatchList(it.watchList)
                        // Reload the watchlist to update the UI
                        loadWatchlist(it.watchList.userId)
                    }
                }
            } catch (e: Exception) {
                _watchlistState.value = WatchListState.Error(e.message ?: "Failed to remove item")
            }
        }
    }

    fun addToWatchlist(userId: Int, itemId: Int, itemType: String) {
        viewModelScope.launch {
            try {
                val watchlistItem = WatchList(
                    userId = userId,
                    itemId = itemId,
                    itemType = itemType
                )
                watchListRepository.addToWatchList(watchlistItem)
                loadWatchlist(userId) // Refresh the list
            } catch (e: Exception) {
                _watchlistState.value = WatchListState.Error(e.message ?: "Failed to add to watchlist")
            }
        }
    }

    suspend fun isItemInWatchlist(userId: Int, itemId: Int, itemType: String): Boolean {
        return watchListRepository.isItemInWatchlist(userId, itemId, itemType)
    }

    fun toggleWatchlistItem(userId: Int, itemId: Int, itemType: String) {
        viewModelScope.launch {
            try {
                watchListRepository.toggleWatchListItem(userId, itemId, itemType)
                loadWatchlist(userId) // Refresh the list
            } catch (e: Exception) {
                _watchlistState.value = WatchListState.Error(e.message ?: "Failed to update watchlist")
            }
        }
    }
} 