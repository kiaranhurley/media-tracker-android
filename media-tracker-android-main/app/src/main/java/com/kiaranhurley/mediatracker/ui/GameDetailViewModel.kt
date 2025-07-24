package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.repository.GameRepository
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

sealed class GameDetailState {
    object Idle : GameDetailState()
    object Loading : GameDetailState()
    data class Success(
        val game: Game,
        val userReview: Review? = null,
        val reviews: List<Review> = emptyList(),
        val isInWatchlist: Boolean = false,
        val userRating: Rating? = null
    ) : GameDetailState()
    data class Error(val message: String) : GameDetailState()
}

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository,
    private val watchListRepository: WatchListRepository
) : ViewModel() {
    private val _detailState = MutableStateFlow<GameDetailState>(GameDetailState.Idle)
    val detailState: StateFlow<GameDetailState> = _detailState.asStateFlow()
    
    private val _reviewsState = MutableStateFlow<GameDetailState>(GameDetailState.Idle)
    val reviewsState: StateFlow<GameDetailState> = _reviewsState.asStateFlow()
    
    private var currentUserId: Int = 1 // Default user ID

    fun loadGameDetails(gameId: Int, userId: Int) {
        currentUserId = userId
        _detailState.value = GameDetailState.Loading
        viewModelScope.launch {
            try {
                // First try to get game by local ID
                var game = gameRepository.getGameById(gameId)
                
                // If not found locally, try to get from API using IGDB ID
                if (game == null) {
                    // Assume gameId might be an IGDB ID if not found locally
                    game = gameRepository.getGameDetailsFromApi(gameId)
                }
                
                if (game != null) {
                    // Check if game is in user's watchlist
                    val isInWatchlist = watchListRepository.isItemInWatchlist(userId, gameId, "GAME")
                    
                    // Get user's existing rating for this game
                    val existingRating = ratingRepository.getUserRating(userId, gameId, "GAME")
                    
                    // Get user's existing review for this game
                    val existingReview = reviewRepository.getUserReviewForItem(userId, gameId, "GAME")
                    
                    _detailState.value = GameDetailState.Success(
                        game = game,
                        isInWatchlist = isInWatchlist,
                        userRating = existingRating,
                        userReview = existingReview
                    )
                } else {
                    _detailState.value = GameDetailState.Error("Game not found")
                }
            } catch (e: Exception) {
                _detailState.value = GameDetailState.Error(e.message ?: "Failed to load game details")
            }
        }
    }
    
    fun loadGameReviews(gameId: Int) {
        _reviewsState.value = GameDetailState.Loading
        viewModelScope.launch {
            try {
                val reviews = reviewRepository.getReviewsForItem(gameId, "GAME")
                _reviewsState.value = GameDetailState.Success(
                    game = Game(
                        gameId = gameId,
                        igdbId = 0,
                        name = "",
                        summary = null,
                        firstReleaseDate = null,
                        aggregatedRating = null,
                        aggregatedRatingCount = null,
                        coverId = null,
                        coverUrl = null,
                        platforms = null,
                        developer = null,
                        publisher = null
                    ),
                    reviews = reviews
                )
            } catch (e: Exception) {
                _reviewsState.value = GameDetailState.Error(e.message ?: "Failed to load reviews")
            }
        }
    }
    
    fun rateGame(userId: Int, gameId: Int, rating: Float) {
        viewModelScope.launch {
            try {
                val existingRating = ratingRepository.getUserRating(userId, gameId, "GAME")
                if (existingRating != null) {
                    val updatedRating = existingRating.copy(
                        rating = rating,
                        updatedAt = Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                } else {
                    val ratingEntity = Rating(
                        userId = userId,
                        itemId = gameId,
                        itemType = "GAME",
                        rating = rating,
                        createdAt = Date()
                    )
                    ratingRepository.setRating(ratingEntity)
                }
                // Refresh game details to update the rating state
                loadGameDetails(gameId, currentUserId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun submitReview(userId: Int, gameId: Int, content: String, rating: Float) {
        viewModelScope.launch {
            try {
                // Check if user already has a review for this game
                val existingReview = reviewRepository.getUserReviewForItem(userId, gameId, "GAME")
                
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
                        itemId = gameId,
                        itemType = "GAME",
                        title = null,
                        content = content,
                        rating = rating,
                        createdAt = Date()
                    )
                    reviewRepository.addReview(review)
                }
                
                // Also update the rating
                val existingRating = ratingRepository.getUserRating(userId, gameId, "GAME")
                if (existingRating != null) {
                    val updatedRating = existingRating.copy(
                        rating = rating,
                        updatedAt = Date()
                    )
                    ratingRepository.updateRating(updatedRating)
                } else {
                    val ratingEntity = Rating(
                        userId = userId,
                        itemId = gameId,
                        itemType = "GAME",
                        rating = rating,
                        createdAt = Date()
                    )
                    ratingRepository.setRating(ratingEntity)
                }
                
                // Refresh game details and reviews
                loadGameDetails(gameId, currentUserId)
                loadGameReviews(gameId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteReview(userId: Int, gameId: Int) {
        viewModelScope.launch {
            try {
                reviewRepository.deleteUserReviewForItem(userId, gameId, "GAME")
                loadGameReviews(gameId) // Refresh reviews
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
                    // Reload game details to refresh the review
                    val currentState = _detailState.value
                    if (currentState is GameDetailState.Success) {
                        loadGameDetails(currentState.game.gameId, currentUserId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun toggleWatchlist(userId: Int, gameId: Int, addToWatchlist: Boolean) {
        viewModelScope.launch {
            try {
                if (addToWatchlist) {
                    val watchlistItem = WatchList(
                        userId = userId,
                        itemId = gameId,
                        itemType = "GAME",
                        addedAt = Date()
                    )
                    watchListRepository.addToWatchList(watchlistItem)
                } else {
                    val existingItem = watchListRepository.getWatchListItem(userId, gameId, "GAME")
                    if (existingItem != null) {
                        watchListRepository.removeFromWatchList(existingItem)
                    }
                }
                // Refresh game details to update the watchlist state
                loadGameDetails(gameId, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 