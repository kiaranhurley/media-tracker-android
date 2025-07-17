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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Success(
        val user: User?,
        val filmCount: Int,
        val gameCount: Int,
        val ratingCount: Int,
        val reviewCount: Int,
        val recentReviews: List<Review>,
        val recentRatings: List<Rating>
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val filmRepository: FilmRepository,
    private val gameRepository: GameRepository,
    private val ratingRepository: RatingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Idle)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun loadHome(userId: Int) {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                val films = filmRepository.getAllFilmsOrderedByPopularity().firstOrNull() ?: emptyList()
                val games = gameRepository.getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
                val userRatings = ratingRepository.getUserRatings(userId).firstOrNull() ?: emptyList()
                val userReviews = reviewRepository.getUserReviews(userId).firstOrNull() ?: emptyList()
                
                val filmCount = films.size
                val gameCount = games.size
                val ratingCount = userRatings.size
                val reviewCount = userReviews.size
                val recentReviews = userReviews.take(5)
                val recentRatings = userRatings.take(5)
                
                _homeState.value = HomeState.Success(
                    user = user,
                    filmCount = filmCount,
                    gameCount = gameCount,
                    ratingCount = ratingCount,
                    reviewCount = reviewCount,
                    recentReviews = recentReviews,
                    recentRatings = recentRatings
                )
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
} 