package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import com.kiaranhurley.mediatracker.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FilmDetailState {
    object Idle : FilmDetailState()
    object Loading : FilmDetailState()
    data class Success(
        val film: Film?,
        val reviews: List<Review>,
        val averageRating: Float?
    ) : FilmDetailState()
    data class Error(val message: String) : FilmDetailState()
}

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow<FilmDetailState>(FilmDetailState.Idle)
    val state: StateFlow<FilmDetailState> = _state.asStateFlow()

    fun loadFilmDetails(filmId: Int) {
        _state.value = FilmDetailState.Loading
        viewModelScope.launch {
            try {
                val film = filmRepository.getFilmById(filmId)
                val reviews = if (film != null) reviewRepository.getReviewsForItem(film.filmId, "FILM") else emptyList()
                val avgRating = if (film != null) ratingRepository.getAverageRating(film.filmId, "FILM") else null
                _state.value = FilmDetailState.Success(film, reviews, avgRating)
            } catch (e: Exception) {
                _state.value = FilmDetailState.Error(e.message ?: "Failed to load film details")
            }
        }
    }
} 