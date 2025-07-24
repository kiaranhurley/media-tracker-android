package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.repository.UserRepository
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.GameRepository
import com.kiaranhurley.mediatracker.repository.RatingRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import com.kiaranhurley.mediatracker.repository.WatchListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewWithContent(
    val review: Review,
    val contentTitle: String,
    val contentType: String // "FILM" or "GAME"
)

data class RatingWithContent(
    val rating: Rating,
    val contentTitle: String,
    val contentType: String // "FILM" or "GAME"
)

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Success(
        val user: User?,
        val filmCount: Int,
        val gameCount: Int,
        val ratingCount: Int,
        val reviewCount: Int,
        val recentReviews: List<ReviewWithContent>,
        val recentRatings: List<RatingWithContent>
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository,
    private val ratingRepository: RatingRepository,
    private val reviewRepository: ReviewRepository,
    private val watchListRepository: WatchListRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Idle)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun loadHome(userId: Int) {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                val userRatings = ratingRepository.getUserRatings(userId).firstOrNull() ?: emptyList()
                val userReviews = reviewRepository.getUserReviews(userId).firstOrNull() ?: emptyList()
                val userWatchList = watchListRepository.getUserWatchList(userId).firstOrNull() ?: emptyList()
                
                // Get unique film and game IDs for collection count
                val filmIds = (userRatings.filter { it.itemType == "FILM" }.map { it.itemId } + userWatchList.filter { it.itemType == "FILM" }.map { it.itemId }).toSet()
                val gameIds = (userRatings.filter { it.itemType == "GAME" }.map { it.itemId } + userWatchList.filter { it.itemType == "GAME" }.map { it.itemId }).toSet()
                
                // Get films and games for lookup (only what's needed for display)
                val films = filmRepository.getAllFilmsOrderedByPopularity().firstOrNull() ?: emptyList()
                val games = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                
                // Create maps for quick lookup
                val filmMap = films.associateBy { it.filmId }
                val gameMap = games.associateBy { it.gameId }
                
                // Enrich ratings with content titles
                val recentRatingsWithContent = userRatings.take(5).mapNotNull { rating ->
                    val contentTitle = when (rating.itemType) {
                        "FILM" -> {
                            val film = filmMap[rating.itemId] 
                                ?: filmRepository.getFilmById(rating.itemId)
                                ?: filmRepository.getFilmByTmdbId(rating.itemId)
                            film?.title ?: "Film #${rating.itemId}"
                        }
                        "GAME" -> {
                            val game = gameMap[rating.itemId] 
                                ?: gameRepository.getGameById(rating.itemId)
                                ?: gameRepository.getGameByIgdbId(rating.itemId)
                            game?.name ?: "Game #${rating.itemId}"
                        }
                        else -> "Unknown Content"
                    }
                    RatingWithContent(rating, contentTitle, rating.itemType)
                }
                
                // Enrich reviews with content titles
                val recentReviewsWithContent = userReviews.take(5).mapNotNull { review ->
                    val contentTitle = when (review.itemType) {
                        "FILM" -> {
                            val film = filmMap[review.itemId] 
                                ?: filmRepository.getFilmById(review.itemId)
                                ?: filmRepository.getFilmByTmdbId(review.itemId)
                            film?.title ?: "Film #${review.itemId}"
                        }
                        "GAME" -> {
                            val game = gameMap[review.itemId] 
                                ?: gameRepository.getGameById(review.itemId)
                                ?: gameRepository.getGameByIgdbId(review.itemId)
                            game?.name ?: "Game #${review.itemId}"
                        }
                        else -> "Unknown Content"
                    }
                    ReviewWithContent(review, contentTitle, review.itemType)
                }
                
                // Calculate user's personal library counts (unique only)
                val filmCount = filmIds.size
                val gameCount = gameIds.size
                val ratingCount = userRatings.size
                val reviewCount = userReviews.size
                
                _homeState.value = HomeState.Success(
                    user = user,
                    filmCount = filmCount,
                    gameCount = gameCount,
                    ratingCount = ratingCount,
                    reviewCount = reviewCount,
                    recentReviews = recentReviewsWithContent,
                    recentRatings = recentRatingsWithContent
                )
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            try {
                val review = reviewRepository.getReviewById(reviewId)
                if (review != null) {
                    reviewRepository.deleteReview(review)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(review.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun editReview(reviewId: Int, newContent: String, newRating: Float) {
        viewModelScope.launch {
            try {
                val review = reviewRepository.getReviewById(reviewId)
                if (review != null) {
                    val updatedReview = review.copy(
                        content = newContent,
                        rating = newRating,
                        updatedAt = java.util.Date()
                    )
                    reviewRepository.updateReview(updatedReview)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(review.userId)
                    }
                }
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
                        updatedAt = java.util.Date()
                    )
                    reviewRepository.updateReview(updatedReview)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(review.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteRating(ratingId: Int) {
        viewModelScope.launch {
            try {
                val rating = ratingRepository.getRatingById(ratingId)
                if (rating != null) {
                    ratingRepository.deleteRating(rating)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(rating.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun editRating(ratingId: Int, newRating: Float) {
        viewModelScope.launch {
            try {
                val rating = ratingRepository.getRatingById(ratingId)
                if (rating != null) {
                    val updatedRating = rating.copy(
                        rating = newRating,
                        updatedAt = java.util.Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(rating.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleRatingPrivacy(ratingId: Int) {
        viewModelScope.launch {
            try {
                val rating = ratingRepository.getRatingById(ratingId)
                if (rating != null) {
                    val updatedRating = rating.copy(
                        isPrivate = !rating.isPrivate,
                        updatedAt = java.util.Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                    // Reload home data to refresh the activity feed
                    val currentState = _homeState.value
                    if (currentState is HomeState.Success) {
                        loadHome(rating.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 