package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.async

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val films: List<Film>, val games: List<Game>) : SearchState()
    data class Error(val message: String) : SearchState()
    object Empty : SearchState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }
        
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                // Search both films and games concurrently
                val filmsDeferred = async {
                    try {
                        filmRepository.searchFilmsFromApi(query)
                    } catch (e: Exception) {
                        // Fallback to local search if API fails
                        filmRepository.searchFilms(query)
                    }
                }
                
                val gamesDeferred = async {
                    try {
                        gameRepository.searchGamesFromApi(query)
                    } catch (e: Exception) {
                        // Fallback to local search if API fails
                        gameRepository.searchGames(query)
                    }
                }
                
                val films = filmsDeferred.await()
                val games = gamesDeferred.await()
                
                if (films.isEmpty() && games.isEmpty()) {
                    _searchState.value = SearchState.Empty
                } else {
                    _searchState.value = SearchState.Success(films, games)
                }
                
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Search failed")
            }
        }
    }
    
    fun clearSearch() {
        _searchState.value = SearchState.Idle
    }
} 