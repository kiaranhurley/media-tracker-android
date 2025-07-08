package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.repository.GameRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import com.kiaranhurley.mediatracker.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GameDetailState {
    object Idle : GameDetailState()
    object Loading : GameDetailState()
    data class Success(
        val game: Game?,
        val reviews: List<Review>,
        val averageRating: Float?
    ) : GameDetailState()
    data class Error(val message: String) : GameDetailState()
}

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow<GameDetailState>(GameDetailState.Idle)
    val state: StateFlow<GameDetailState> = _state.asStateFlow()

    fun loadGameDetails(gameId: Int) {
        _state.value = GameDetailState.Loading
        viewModelScope.launch {
            try {
                val game = gameRepository.getGameById(gameId)
                val reviews = if (game != null) reviewRepository.getReviewsForItem(game.gameId, "GAME") else emptyList()
                val avgRating = if (game != null) ratingRepository.getAverageRating(game.gameId, "GAME") else null
                _state.value = GameDetailState.Success(game, reviews, avgRating)
            } catch (e: Exception) {
                _state.value = GameDetailState.Error(e.message ?: "Failed to load game details")
            }
        }
    }
} 