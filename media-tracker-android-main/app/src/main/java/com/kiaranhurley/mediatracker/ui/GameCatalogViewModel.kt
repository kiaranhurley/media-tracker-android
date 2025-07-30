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
                
                // Test basic API connection first
                val apiWorking = try {
                    println("DEBUG: GameCatalogViewModel - Testing basic API connection...")
                    gameRepository.testBasicApiConnection()
                } catch (e: Exception) {
                    println("DEBUG: GameCatalogViewModel - API test failed: ${e.message}")
                    false
                }
                
                println("DEBUG: GameCatalogViewModel - API connection test result: $apiWorking")
                
                // Test simple IGDB query first
                println("DEBUG: Testing simple IGDB query first...")
                val testGames = gameRepository.testSimpleQuery()
                println("DEBUG: Simple test returned ${testGames.size} games")
                
                val games = if (testGames.isNotEmpty()) {
                    println("DEBUG: Basic connectivity works, trying popular games...")
                    try {
                        val apiGames = gameRepository.getPopularGamesFromApi()
                        println("DEBUG: GameCatalogViewModel - Popular games API returned ${apiGames.size} games")
                        apiGames.ifEmpty { testGames } // Use test games as fallback
                    } catch (e: Exception) {
                        println("DEBUG: Popular games failed, using test results: ${e.message}")
                        testGames
                    }
                } else {
                    println("DEBUG: Basic connectivity failed, trying fallback options...")
                    try {
                        // Try manual token test as fallback
                        println("DEBUG: GameCatalogViewModel - Trying manual token test...")
                        val manualGames = gameRepository.testManualToken()
                        println("DEBUG: GameCatalogViewModel - Manual token test returned ${manualGames.size} games")
                        
                        if (manualGames.isNotEmpty()) {
                            manualGames
                        } else {
                            // Finally try local data
                            println("DEBUG: GameCatalogViewModel - All API methods failed, trying local data...")
                            val localGames = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                            println("DEBUG: GameCatalogViewModel - Local database has ${localGames.size} games")
                            localGames
                        }
                    } catch (e: Exception) {
                        println("DEBUG: GameCatalogViewModel - Fallback failed: ${e.message}")
                        e.printStackTrace()
                        // Final fallback to local data
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