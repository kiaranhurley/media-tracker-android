package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(username: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = userRepository.authenticateUser(username, password)
            if (user != null) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Invalid username or password")
            }
        }
    }

    fun register(user: User) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val usernameTaken = userRepository.isUsernameTaken(user.username)
            val emailRegistered = userRepository.isEmailRegistered(user.email)
            if (usernameTaken) {
                _authState.value = AuthState.Error("Username already taken")
                return@launch
            }
            if (emailRegistered) {
                _authState.value = AuthState.Error("Email already registered")
                return@launch
            }
            val userId = userRepository.insertUser(user)
            val newUser = userRepository.getUserById(userId.toInt())
            if (newUser != null) {
                _authState.value = AuthState.Success(newUser)
            } else {
                _authState.value = AuthState.Error("Registration failed")
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
} 