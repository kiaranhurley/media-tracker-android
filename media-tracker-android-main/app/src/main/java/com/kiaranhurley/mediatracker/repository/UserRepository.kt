package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.UserDao
import com.kiaranhurley.mediatracker.database.entities.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    /**
     * Get a user by their ID
     */
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }
    
    /**
     * Get a user by their username
     */
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    /**
     * Insert a new user
     */
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
    
    /**
     * Get all users as a Flow for reactive updates
     */
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Check if a username is already taken
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }
    
    /**
     * Authenticate user with username and password
     */
    suspend fun authenticateUser(username: String, password: String): User? {
        return userDao.getUserByCredentials(username, password)
    }

    suspend fun updateDisplayName(newName: String) {
        userDao.updateDisplayName(newName)
    }
} 