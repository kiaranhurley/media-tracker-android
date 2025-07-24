package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.repository.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FilmCatalogState {
    object Idle : FilmCatalogState()
    object Loading : FilmCatalogState()
    data class Success(val films: List<Film>) : FilmCatalogState()
    data class Error(val message: String) : FilmCatalogState()
}

@HiltViewModel
class FilmCatalogViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : ViewModel() {
    private val _state = MutableStateFlow<FilmCatalogState>(FilmCatalogState.Idle)
    val state: StateFlow<FilmCatalogState> = _state.asStateFlow()

    init {
        loadAllFilms()
    }

    fun loadAllFilms() {
        _state.value = FilmCatalogState.Loading
        viewModelScope.launch {
            try {
                // Try to get popular films from API first, then fallback to local
                val films = try {
                    filmRepository.getPopularFilmsFromApi()
                } catch (e: Exception) {
                    // Fallback to local data if API fails
                    filmRepository.getAllFilmsOrderedByPopularity().firstOrNull() ?: emptyList()
                    }
                        _state.value = FilmCatalogState.Success(films)
            } catch (e: Exception) {
                _state.value = FilmCatalogState.Error(e.message ?: "Failed to load films")
            }
        }
    }

    fun searchFilms(query: String) {
        if (query.isBlank()) {
            loadAllFilms()
            return
        }
        
        _state.value = FilmCatalogState.Loading
        viewModelScope.launch {
            try {
                // Try API search first, then fallback to local search
                val films = try {
                    filmRepository.searchFilmsFromApi(query)
                } catch (e: Exception) {
                    // Fallback to local search if API fails
                    filmRepository.searchFilms(query)
                }
                _state.value = FilmCatalogState.Success(films)
            } catch (e: Exception) {
                _state.value = FilmCatalogState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun searchFilmsFromApi(query: String) {
        if (query.isBlank()) {
            loadAllFilms()
            return
        }
        
        _state.value = FilmCatalogState.Loading
        viewModelScope.launch {
            try {
                val films = filmRepository.searchFilmsFromApi(query)
                _state.value = FilmCatalogState.Success(films)
            } catch (e: Exception) {
                _state.value = FilmCatalogState.Error(e.message ?: "API search failed")
                // Fallback to local search
                try {
                    val localFilms = filmRepository.searchFilms(query)
                    _state.value = FilmCatalogState.Success(localFilms)
                } catch (fallbackException: Exception) {
                    _state.value = FilmCatalogState.Error("Search failed: ${fallbackException.message}")
                }
            }
        }
    }

    fun loadPopularFilms() {
        _state.value = FilmCatalogState.Loading
        viewModelScope.launch {
            try {
                val films = filmRepository.getPopularFilmsFromApi()
                _state.value = FilmCatalogState.Success(films)
            } catch (e: Exception) {
                _state.value = FilmCatalogState.Error(e.message ?: "Failed to load popular films")
                // Fallback to local films
                loadAllFilms()
            }
        }
    }

    fun refresh() {
        loadAllFilms()
    }
} 