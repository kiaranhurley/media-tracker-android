package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GameCatalogState {
    object Idle : GameCatalogState()
    object Loading : GameCatalogState()
    data class Success(val games: List<Game>) : GameCatalogState()
    data class Error(val message: String) : GameCatalogState()
}

@HiltViewModel
class GameCatalogViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {
    private val _state = MutableStateFlow<GameCatalogState>(GameCatalogState.Idle)
    val state: StateFlow<GameCatalogState> = _state.asStateFlow()

    init {
        println("DEBUG: GameCatalogViewModel initialized")
        loadGames()
    }

    fun loadGames() {
        println("DEBUG: GameCatalogViewModel - Loading games...")
        println("DEBUG: GameCatalogViewModel - Current thread: ${Thread.currentThread().name}")
        _state.value = GameCatalogState.Loading
        viewModelScope.launch {
            try {
                println("DEBUG: GameCatalogViewModel - Inside coroutine, thread: ${Thread.currentThread().name}")
                
                // Try to get popular games from API first, then fallback to local
                val games = try {
                    println("DEBUG: GameCatalogViewModel - Attempting to get popular games from API...")
                    val apiGames = gameRepository.getPopularGamesFromApi()
                    println("DEBUG: GameCatalogViewModel - API returned ${apiGames.size} games")
                    
                    // If no games from API, try manual token test
                    if (apiGames.isEmpty()) {
                        println("DEBUG: GameCatalogViewModel - No games from API, trying manual token test...")
                        val manualGames = gameRepository.testManualToken()
                        println("DEBUG: GameCatalogViewModel - Manual token test returned ${manualGames.size} games")
                        
                        if (manualGames.isNotEmpty()) {
                            manualGames
                        } else {
                            // Finally try local data
                            println("DEBUG: GameCatalogViewModel - Manual token failed, trying local data...")
                            val localGames = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                            println("DEBUG: GameCatalogViewModel - Local database has ${localGames.size} games")
                            localGames
                        }
                    } else {
                        apiGames
                    }
                } catch (e: Exception) {
                    println("DEBUG: GameCatalogViewModel - API failed with exception: ${e.javaClass.simpleName}: ${e.message}")
                    e.printStackTrace()
                    
                    // Try manual token as fallback
                    try {
                        println("DEBUG: GameCatalogViewModel - Trying manual token fallback...")
                        val manualGames = gameRepository.testManualToken()
                        println("DEBUG: GameCatalogViewModel - Manual token fallback returned ${manualGames.size} games")
                        if (manualGames.isNotEmpty()) {
                            manualGames
                        } else {
                            // Finally fallback to local data if manual token fails
                            println("DEBUG: GameCatalogViewModel - Manual token failed, trying local fallback...")
                            val localGames = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                            println("DEBUG: GameCatalogViewModel - Local fallback has ${localGames.size} games")
                            localGames
                        }
                    } catch (manualException: Exception) {
                        println("DEBUG: GameCatalogViewModel - Manual token also failed with: ${manualException.javaClass.simpleName}: ${manualException.message}")
                        manualException.printStackTrace()
                        // Final fallback to local data
                        println("DEBUG: GameCatalogViewModel - Trying final local fallback...")
                        val localGames = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                        println("DEBUG: GameCatalogViewModel - Final local fallback has ${localGames.size} games")
                        localGames
                    }
                }
                
                println("DEBUG: GameCatalogViewModel - Final result: ${games.size} games")
                games.forEachIndexed { index, game ->
                    println("DEBUG: GameCatalogViewModel - Game ${index + 1}: ${game.name} (ID: ${game.gameId}, IGDB: ${game.igdbId})")
                }
                
                println("DEBUG: GameCatalogViewModel - Setting success state with ${games.size} games")
                _state.value = GameCatalogState.Success(games)
            } catch (e: Exception) {
                println("DEBUG: GameCatalogViewModel - Final error loading games: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                _state.value = GameCatalogState.Error(e.message ?: "Failed to load games")
            }
        }
    }

    fun searchGames(query: String) {
        println("DEBUG: Searching games for: '$query'")
        _state.value = GameCatalogState.Loading
        viewModelScope.launch {
            try {
                // Try API search first, then fallback to local search
                val games = try {
                    println("DEBUG: Attempting API search for games...")
                    val apiGames = gameRepository.searchGamesFromApi(query)
                    println("DEBUG: API search returned ${apiGames.size} games")
                    apiGames
                } catch (e: Exception) {
                    println("DEBUG: API search failed, falling back to local search: ${e.message}")
                    // Fallback to local search if API fails
                    gameRepository.searchGames(query)
                }
                
                println("DEBUG: Setting search success state with ${games.size} games")
                _state.value = GameCatalogState.Success(games)
            } catch (e: Exception) {
                println("DEBUG: Error searching games: ${e.message}")
                e.printStackTrace()
                _state.value = GameCatalogState.Error(e.message ?: "Search failed")
            }
        }
    }
} 