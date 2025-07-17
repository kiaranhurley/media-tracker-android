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

    fun loadGames() {
        _state.value = GameCatalogState.Loading
        viewModelScope.launch {
            try {
                val games = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                _state.value = GameCatalogState.Success(games)
            } catch (e: Exception) {
                _state.value = GameCatalogState.Error(e.message ?: "Failed to load games")
            }
        }
    }

    fun searchGames(query: String) {
        _state.value = GameCatalogState.Loading
        viewModelScope.launch {
            try {
                val games = gameRepository.searchGames(query)
                _state.value = GameCatalogState.Success(games)
            } catch (e: Exception) {
                _state.value = GameCatalogState.Error(e.message ?: "Search failed")
            }
        }
    }
} 