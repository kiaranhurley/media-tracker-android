package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.repository.RatingRepository
import com.kiaranhurley.mediatracker.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReviewState {
    object Idle : ReviewState()
    object Loading : ReviewState()
    object Success : ReviewState()
    data class Error(val message: String) : ReviewState()
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ReviewState>(ReviewState.Idle)
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    fun submitReview(review: Review, rating: Rating) {
        _state.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                reviewRepository.addReview(review)
                ratingRepository.setRating(rating)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Failed to submit review")
            }
        }
    }

    fun updateReview(review: Review, rating: Rating) {
        _state.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                reviewRepository.updateReview(review)
                ratingRepository.updateRating(rating)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Failed to update review")
            }
        }
    }

    fun deleteReview(review: Review, rating: Rating) {
        _state.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(review)
                ratingRepository.deleteRating(rating)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Failed to delete review")
            }
        }
    }

    fun toggleReviewPrivacy(review: Review) {
        _state.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                val updatedReview = review.copy(isPrivate = !review.isPrivate)
                reviewRepository.updateReview(updatedReview)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Failed to toggle review privacy")
            }
        }
    }

    fun toggleRatingPrivacy(rating: Rating) {
        _state.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                val updatedRating = rating.copy(isPrivate = !rating.isPrivate)
                ratingRepository.updateRating(updatedRating)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Failed to toggle rating privacy")
            }
        }
    }
} 