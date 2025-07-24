package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.repository.UserRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import com.kiaranhurley.mediatracker.repository.RatingRepository
import com.kiaranhurley.mediatracker.repository.WatchListRepository
import com.kiaranhurley.mediatracker.repository.CustomListRepository
import com.kiaranhurley.mediatracker.repository.FilmRepository
import com.kiaranhurley.mediatracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(
        val user: User?,
        val reviews: List<ReviewWithContent>,
        val ratings: List<RatingWithContent>,
        val watchList: List<WatchListWithContent>,
        val customLists: List<CustomList>
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

data class WatchListWithContent(
    val watchList: WatchList,
    val title: String,
    val posterUrl: String?
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository,
    private val watchListRepository: WatchListRepository,
    private val customListRepository: CustomListRepository,
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val state: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfile(userId: Int) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                val userRatings = ratingRepository.getUserRatings(userId).firstOrNull() ?: emptyList()
                val userReviews = reviewRepository.getUserReviews(userId).firstOrNull() ?: emptyList()
                val userWatchList = watchListRepository.getUserWatchList(userId).firstOrNull() ?: emptyList()
                val userCustomLists = customListRepository.getUserLists(userId).firstOrNull() ?: emptyList()
                
                // Get films and games for lookup
                val films = filmRepository.getAllFilmsOrderedByPopularity().firstOrNull() ?: emptyList()
                val games = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                
                // Create maps for quick lookup
                val filmMap = films.associateBy { it.filmId }
                val gameMap = games.associateBy { it.gameId }
                
                // Enrich ratings with content titles
                val ratingsWithContent = userRatings.mapNotNull { rating ->
                    val contentTitle = when (rating.itemType) {
                        "FILM" -> {
                            // Try local ID first, then TMDB ID
                            val film = filmMap[rating.itemId] 
                                ?: filmRepository.getFilmById(rating.itemId)
                                ?: filmRepository.getFilmByTmdbId(rating.itemId)
                            film?.title ?: "Film #${rating.itemId}"
                        }
                        "GAME" -> {
                            // Try local ID first, then IGDB ID
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
                val reviewsWithContent = userReviews.mapNotNull { review ->
                    val contentTitle = when (review.itemType) {
                        "FILM" -> {
                            // Try local ID first, then TMDB ID
                            val film = filmMap[review.itemId] 
                                ?: filmRepository.getFilmById(review.itemId)
                                ?: filmRepository.getFilmByTmdbId(review.itemId)
                            film?.title ?: "Film #${review.itemId}"
                        }
                        "GAME" -> {
                            // Try local ID first, then IGDB ID
                            val game = gameMap[review.itemId] 
                                ?: gameRepository.getGameById(review.itemId)
                                ?: gameRepository.getGameByIgdbId(review.itemId)
                            game?.name ?: "Game #${review.itemId}"
                        }
                        else -> "Unknown Content"
                    }
                    ReviewWithContent(review, contentTitle, review.itemType)
                }
                
                // Enrich watchlist with title and poster
                val watchListWithContent = userWatchList.map { item ->
                    when (item.itemType) {
                        "FILM" -> {
                            val film = filmMap[item.itemId] 
                                ?: filmRepository.getFilmById(item.itemId)
                                ?: filmRepository.getFilmByTmdbId(item.itemId)
                            WatchListWithContent(
                                watchList = item,
                                title = film?.title ?: "Film #${item.itemId}",
                                posterUrl = film?.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
                            )
                        }
                        "GAME" -> {
                            val game = gameMap[item.itemId] 
                                ?: gameRepository.getGameById(item.itemId)
                                ?: gameRepository.getGameByIgdbId(item.itemId)
                            WatchListWithContent(
                                watchList = item,
                                title = game?.name ?: "Game #${item.itemId}",
                                posterUrl = game?.coverUrl
                            )
                        }
                        else -> WatchListWithContent(item, "Unknown Content", null)
                    }
                }
                
                val filmCount = films.size
                val gameCount = games.size
                val ratingCount = userRatings.size
                val reviewCount = userReviews.size
                
                _profileState.value = ProfileState.Success(
                    user = user,
                    reviews = reviewsWithContent,
                    ratings = ratingsWithContent,
                    watchList = watchListWithContent,
                    customLists = userCustomLists
                )
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            try {
                val review = reviewRepository.getReviewById(reviewId)
                if (review != null) {
                    reviewRepository.deleteReview(review)
                    // Reload profile data to refresh the reviews list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(review.userId)
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
                    // Reload profile data to refresh the reviews list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(review.userId)
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
                    // Reload profile data to refresh the reviews list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(review.userId)
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
                    // Reload profile data to refresh the ratings list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(rating.userId)
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
                    // Reload profile data to refresh the ratings list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(rating.userId)
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
                    // Reload profile data to refresh the ratings list
                    val currentState = _profileState.value
                    if (currentState is ProfileState.Success) {
                        loadProfile(rating.userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun clearAllGames() {
        viewModelScope.launch {
            try {
                gameRepository.deleteAllGames()
                filmRepository.deleteAllFilms()
                // Reload profile data to refresh the stats
                val currentState = _profileState.value
                if (currentState is ProfileState.Success) {
                    loadProfile(currentState.user?.userId ?: 1)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 