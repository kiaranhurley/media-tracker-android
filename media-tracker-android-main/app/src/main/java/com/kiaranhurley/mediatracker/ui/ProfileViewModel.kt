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
        val reviews: List<Review>,
        val ratings: List<Rating>,
        val watchList: List<WatchList>,
        val customLists: List<CustomList>
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val ratingRepository: RatingRepository,
    private val watchListRepository: WatchListRepository,
    private val customListRepository: CustomListRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadProfile(userId: Int) {
        _state.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                val reviews = reviewRepository.getUserReviews(userId).firstOrNull() ?: emptyList()
                val ratings = ratingRepository.getUserRatings(userId).firstOrNull() ?: emptyList()
                val watchList = watchListRepository.getUserWatchList(userId).firstOrNull() ?: emptyList()
                val customLists = customListRepository.getUserLists(userId).firstOrNull() ?: emptyList()
                _state.value = ProfileState.Success(user, reviews, ratings, watchList, customLists)
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }
} 