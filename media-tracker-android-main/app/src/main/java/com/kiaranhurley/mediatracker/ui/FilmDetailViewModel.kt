package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import com.kiaranhurley.mediatracker.repository.RatingRepository
import com.kiaranhurley.mediatracker.repository.WatchListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

sealed class FilmDetailState {
    object Idle : FilmDetailState()
    object Loading : FilmDetailState()
    data class Success(
        val film: Film,
        val userReview: Review? = null,
        val reviews: List<Review> = emptyList(),
        val isInWatchlist: Boolean = false,
        val userRating: Rating? = null
    ) : FilmDetailState()
    data class Error(val message: String) : FilmDetailState()
}

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository,
    private val watchListRepository: WatchListRepository
) : ViewModel() {
    private val _detailState = MutableStateFlow<FilmDetailState>(FilmDetailState.Idle)
    val detailState: StateFlow<FilmDetailState> = _detailState.asStateFlow()
    
    private val _reviewsState = MutableStateFlow<FilmDetailState>(FilmDetailState.Idle)
    val reviewsState: StateFlow<FilmDetailState> = _reviewsState.asStateFlow()
    
    private var currentUserId: Int = 1 // Default user ID

    fun loadFilmDetails(filmId: Int, userId: Int) {
        currentUserId = userId
        _detailState.value = FilmDetailState.Loading
        viewModelScope.launch {
            try {
                // First try to get film by local ID
                var film = filmRepository.getFilmById(filmId)
                
                // If not found locally, try to get from API using TMDB ID
                if (film == null) {
                    // Assume filmId might be a TMDB ID if not found locally
                    film = filmRepository.getFilmDetailsFromApi(filmId)
                }
                
                if (film != null) {
                    // Check if film is in user's watchlist
                    val isInWatchlist = watchListRepository.isItemInWatchlist(userId, filmId, "FILM")
                    
                    // Get user's existing rating for this film
                    val existingRating = ratingRepository.getUserRating(userId, filmId, "FILM")
                    
                    // Get user's existing review for this film
                    val existingReview = reviewRepository.getUserReviewForItem(userId, filmId, "FILM")
                    
                    _detailState.value = FilmDetailState.Success(
                        film = film,
                        isInWatchlist = isInWatchlist,
                        userRating = existingRating,
                        userReview = existingReview
                    )
                } else {
                    _detailState.value = FilmDetailState.Error("Film not found")
                }
            } catch (e: Exception) {
                _detailState.value = FilmDetailState.Error(e.message ?: "Failed to load film details")
            }
        }
    }
    
    fun loadFilmReviews(filmId: Int) {
        _reviewsState.value = FilmDetailState.Loading
        viewModelScope.launch {
            try {
                val reviews = reviewRepository.getReviewsForItem(filmId, "FILM")
                _reviewsState.value = FilmDetailState.Success(
                    film = Film(
                        filmId = filmId,
                        tmdbId = 0,
                        title = "",
                        releaseDate = Date(),
                        posterPath = null,
                        overview = null,
                        runtime = null,
                        popularity = 0f,
                        voteAverage = 0f,
                        director = null,
                        genres = null,
                        cast = null,
                        productionCompanies = null
                    ),
                    reviews = reviews
                )
            } catch (e: Exception) {
                _reviewsState.value = FilmDetailState.Error(e.message ?: "Failed to load reviews")
            }
        }
    }
    
    fun rateFilm(userId: Int, filmId: Int, rating: Float) {
        viewModelScope.launch {
            try {
                val existingRating = ratingRepository.getUserRating(userId, filmId, "FILM")
                if (existingRating != null) {
                    val updatedRating = existingRating.copy(
                        rating = rating,
                        updatedAt = Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                } else {
                    val ratingEntity = Rating(
                        userId = userId,
                        itemId = filmId,
                        itemType = "FILM",
                        rating = rating,
                        createdAt = Date()
                    )
                    ratingRepository.setRating(ratingEntity)
                }
                // Refresh film details to update the rating state
                loadFilmDetails(filmId, currentUserId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun submitReview(userId: Int, filmId: Int, content: String, rating: Float) {
        viewModelScope.launch {
            try {
                // Check if user already has a review for this film
                val existingReview = reviewRepository.getUserReviewForItem(userId, filmId, "FILM")
                
                if (existingReview != null) {
                    // Update existing review
                    val updatedReview = existingReview.copy(
                        content = content,
                        rating = rating,
                        updatedAt = Date()
                    )
                    reviewRepository.updateReview(updatedReview)
                } else {
                    // Create new review
                    val review = Review(
                        userId = userId,
                        itemId = filmId,
                        itemType = "FILM",
                        title = null,
                        content = content,
                        rating = rating,
                        createdAt = Date()
                    )
                    reviewRepository.addReview(review)
                }
                
                // Also update the rating
                val existingRating = ratingRepository.getUserRating(userId, filmId, "FILM")
                if (existingRating != null) {
                    val updatedRating = existingRating.copy(
                        rating = rating,
                        updatedAt = Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                } else {
                    val ratingEntity = Rating(
                        userId = userId,
                        itemId = filmId,
                        itemType = "FILM",
                        rating = rating,
                        createdAt = Date()
                    )
                    ratingRepository.setRating(ratingEntity)
                }
                
                // Refresh film details and reviews
                loadFilmDetails(filmId, currentUserId)
                loadFilmReviews(filmId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteReview(userId: Int, filmId: Int) {
        viewModelScope.launch {
            try {
                reviewRepository.deleteUserReviewForItem(userId, filmId, "FILM")
                loadFilmReviews(filmId) // Refresh reviews
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleReviewPrivacy(reviewId: Int) {
        viewModelScope.launch {
            try {
                val review = reviewRepository.getReviewById(reviewId)
                if (review != null) {
                    val updatedReview = review.copy(
                        isPrivate = !review.isPrivate,
                        updatedAt = Date()
                    )
                    reviewRepository.updateReview(updatedReview)
                    // Reload film details to refresh the review
                    val currentState = _detailState.value
                    if (currentState is FilmDetailState.Success) {
                        loadFilmDetails(currentState.film.filmId, currentUserId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun toggleWatchlist(userId: Int, filmId: Int, addToWatchlist: Boolean) {
        viewModelScope.launch {
            try {
                if (addToWatchlist) {
                    val watchlistItem = WatchList(
                        userId = userId,
                        itemId = filmId,
                        itemType = "FILM",
                        addedAt = Date()
                    )
                    watchListRepository.addToWatchList(watchlistItem)
                } else {
                    val existingItem = watchListRepository.getWatchListItem(userId, filmId, "FILM")
                    if (existingItem != null) {
                        watchListRepository.removeFromWatchList(existingItem)
                    }
                }
                // Refresh film details to update the watchlist state
                loadFilmDetails(filmId, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 