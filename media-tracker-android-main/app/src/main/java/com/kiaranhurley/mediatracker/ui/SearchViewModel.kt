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

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val films: List<Film>, val games: List<Game>) : SearchState()
    data class Error(val message: String) : SearchState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun search(query: String) {
        _state.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val films = filmRepository.searchFilms(query)
                val games = gameRepository.searchGames(query)
                _state.value = SearchState.Success(films, games)
            } catch (e: Exception) {
                _state.value = SearchState.Error(e.message ?: "Search failed")
            }
        }
    }
} 